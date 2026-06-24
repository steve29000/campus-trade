// Mock API client：当前由内存 db 支撑，返回与后端一致的 ApiResponse 信封。
// 将来把这些方法体换成对 GATEWAY_BASE + EP.* 的 fetch 即可对接真实后端。
import { db, genId } from '../data/db';
import { DEMO_USER_ID } from '../data/seed';
import type {
  User,
  Category,
  Product,
  Verification,
  CampusId,
  Message,
  MessageType,
  Appointment,
  AppointmentStatus,
  Report,
  ReportTargetType,
  ReportReason,
  ReportStatus,
  UserStatus,
} from '../domain/types';
import { transition, type ProductAction } from '../domain/productStatus';
import { estimatePrice, generateDescription, type PriceEstimate } from '../domain/ai';
import { pseudoDistanceKm } from '../domain/campus';
import {
  ok,
  fail,
  type ApiResponse,
  type UserBrief,
  type ProductCard,
  type ProductDetail,
  type ProductQuery,
  type SessionView,
  type ChatThread,
  type AdminStats,
  type VerificationView,
  type ReportView,
} from './types';

const delay = (ms = 140) => new Promise<void>((r) => setTimeout(r, ms));

// ---------- 内部映射 ----------
function brief(u: User | undefined): UserBrief {
  if (!u) {
    return { id: 'unknown', nickname: '已注销用户', avatar: '👤', rating: 0, verifyStatus: 'UNVERIFIED', campus: 'SOUTH' };
  }
  return { id: u.id, nickname: u.nickname, avatar: u.avatar, rating: u.rating, verifyStatus: u.verifyStatus, campus: u.campus };
}

function userById(id: string): User | undefined {
  return db.users.find((u) => u.id === id);
}

function toCard(p: Product): ProductCard {
  return { ...p, seller: brief(userById(p.sellerId)) };
}

// 公开可见的商品状态（浏览列表）
const PUBLIC_STATUSES: Product['status'][] = ['ON_SALE', 'RESERVED'];

export const api = {
  // ======================= 用户 / 认证 =======================
  async login(emailOrId: string): Promise<ApiResponse<User>> {
    await delay();
    const u = db.users.find((x) => x.id === emailOrId || x.email === emailOrId);
    if (!u) return fail(404, '账号不存在，请先注册');
    if (u.status === 'BANNED') return fail(403, '账号已被封禁');
    return ok(u);
  },

  async loginDemo(): Promise<ApiResponse<User>> {
    await delay();
    return ok(userById(DEMO_USER_ID)!);
  },

  async register(payload: { nickname: string; email: string; school: string; campus: CampusId }): Promise<ApiResponse<User>> {
    await delay();
    if (db.users.some((u) => u.email === payload.email)) return fail(409, '邮箱已被注册');
    const now = Date.now();
    const user: User = {
      id: genId('u'),
      nickname: payload.nickname.trim() || '新同学',
      avatar: '🙂',
      email: payload.email,
      school: payload.school,
      campus: payload.campus,
      verifyStatus: 'UNVERIFIED',
      role: 'STUDENT',
      status: 'ACTIVE',
      rating: 5.0,
      createdAt: now,
    };
    db.users.push(user);
    db.persist();
    return ok(user);
  },

  async me(userId: string): Promise<ApiResponse<User>> {
    await delay(60);
    const u = userById(userId);
    return u ? ok(u) : fail(404, '用户不存在');
  },

  async submitVerification(
    userId: string,
    payload: { school: string; campus: CampusId; studentNo: string; proofImage?: string },
  ): Promise<ApiResponse<Verification>> {
    await delay();
    const u = userById(userId);
    if (!u) return fail(404, '用户不存在');
    const now = Date.now();
    const v: Verification = {
      id: genId('v'),
      userId,
      school: payload.school,
      campus: payload.campus,
      studentNo: payload.studentNo,
      proofImage: payload.proofImage,
      status: 'PENDING',
      createdAt: now,
    };
    db.verifications.push(v);
    u.verifyStatus = 'PENDING';
    u.school = payload.school;
    u.campus = payload.campus;
    u.studentNo = payload.studentNo;
    db.persist();
    return ok(v);
  },

  // 审核认证（管理员；演示中也用于"模拟通过"）
  async reviewVerification(verificationId: string, approve: boolean, reason?: string): Promise<ApiResponse<Verification>> {
    await delay();
    const v = db.verifications.find((x) => x.id === verificationId);
    if (!v) return fail(404, '认证申请不存在');
    v.status = approve ? 'VERIFIED' : 'REJECTED';
    v.reviewedAt = Date.now();
    if (!approve) v.rejectReason = reason;
    const u = userById(v.userId);
    if (u) u.verifyStatus = v.status;
    db.persist();
    return ok(v);
  },

  // 便捷封装：审批某用户最近一条认证申请（演示"模拟通过"用）
  async reviewLatestVerification(userId: string, approve: boolean, reason?: string): Promise<ApiResponse<Verification>> {
    const latest = [...db.verifications]
      .filter((x) => x.userId === userId)
      .sort((a, b) => b.createdAt - a.createdAt)[0];
    if (!latest) return fail(404, '没有待审核的认证申请');
    return this.reviewVerification(latest.id, approve, reason);
  },

  // ======================= 分类 =======================
  async listCategories(): Promise<ApiResponse<Category[]>> {
    await delay(50);
    return ok([...db.categories].sort((a, b) => a.sortOrder - b.sortOrder));
  },

  // ======================= 商品 =======================
  async listProducts(query: ProductQuery = {}): Promise<ApiResponse<ProductCard[]>> {
    await delay();
    let items = db.products.filter((p) => PUBLIC_STATUSES.includes(p.status));

    if (query.campus) items = items.filter((p) => p.campus === query.campus);
    if (query.categoryId) items = items.filter((p) => p.categoryId === query.categoryId);
    if (query.condition) items = items.filter((p) => p.conditionLevel === query.condition);
    if (typeof query.minPrice === 'number') items = items.filter((p) => p.price >= query.minPrice!);
    if (typeof query.maxPrice === 'number') items = items.filter((p) => p.price <= query.maxPrice!);
    if (query.keyword) {
      const kw = query.keyword.trim().toLowerCase();
      if (kw) items = items.filter((p) => (p.title + p.description).toLowerCase().includes(kw));
    }

    const viewer = query.viewerCampus ?? 'SOUTH';
    const sort = query.sort ?? 'NEWEST';
    items = [...items].sort((a, b) => {
      switch (sort) {
        case 'PRICE_ASC':
          return a.price - b.price;
        case 'HOT':
          return b.favoriteCount - a.favoriteCount;
        case 'NEAREST':
          return pseudoDistanceKm(viewer, a) - pseudoDistanceKm(viewer, b);
        case 'NEWEST':
        default:
          return b.createdAt - a.createdAt;
      }
    });

    return ok(items.map(toCard));
  },

  async getProduct(id: string): Promise<ApiResponse<ProductDetail>> {
    await delay();
    const p = db.products.find((x) => x.id === id);
    if (!p) return fail(404, '商品不存在或已删除');
    p.viewCount += 1;
    db.persist();
    return ok(toCard(p));
  },

  async myProducts(sellerId: string): Promise<ApiResponse<ProductCard[]>> {
    await delay();
    const items = db.products
      .filter((p) => p.sellerId === sellerId)
      .sort((a, b) => b.updatedAt - a.updatedAt);
    return ok(items.map(toCard));
  },

  async createProduct(payload: {
    sellerId: string;
    title: string;
    description: string;
    categoryId: string;
    price: number;
    originalPrice?: number;
    conditionLevel: Product['conditionLevel'];
    campus: CampusId;
    locationDesc: string;
    negotiable: boolean;
    imageCount: number;
  }): Promise<ApiResponse<ProductCard>> {
    await delay();
    const seller = userById(payload.sellerId);
    if (!seller) return fail(404, '卖家不存在');
    if (seller.verifyStatus !== 'VERIFIED') return fail(403, '请先完成校园认证再发布商品');
    const now = Date.now();
    const product: Product = {
      id: genId('p'),
      sellerId: payload.sellerId,
      title: payload.title.trim(),
      description: payload.description.trim(),
      categoryId: payload.categoryId,
      price: payload.price,
      originalPrice: payload.originalPrice,
      conditionLevel: payload.conditionLevel,
      campus: payload.campus,
      locationDesc: payload.locationDesc,
      negotiable: payload.negotiable,
      status: 'ON_SALE',
      images: Array.from({ length: Math.max(1, payload.imageCount) }, (_, i) => ({ url: '', sortOrder: i })),
      viewCount: 0,
      favoriteCount: 0,
      createdAt: now,
      updatedAt: now,
    };
    db.products.unshift(product);
    db.persist();
    return ok(toCard(product));
  },

  async changeProductStatus(id: string, action: ProductAction, actorId: string): Promise<ApiResponse<ProductCard>> {
    await delay();
    const p = db.products.find((x) => x.id === id);
    if (!p) return fail(404, '商品不存在');
    if (p.sellerId !== actorId) return fail(403, '只有卖家可以操作该商品');
    try {
      p.status = transition(p.status, action);
      p.updatedAt = Date.now();
      // 标记已售出时，关闭相关会话
      if (p.status === 'SOLD') {
        db.sessions.filter((s) => s.productId === p.id && s.status === 'ACTIVE').forEach((s) => (s.status = 'COMPLETED'));
      }
      db.persist();
      return ok(toCard(p));
    } catch (e) {
      return fail(400, (e as Error).message);
    }
  },

  // ======================= 收藏 =======================
  async listFavorites(userId: string): Promise<ApiResponse<ProductCard[]>> {
    await delay();
    const ids = db.favorites.filter((f) => f.userId === userId).sort((a, b) => b.createdAt - a.createdAt).map((f) => f.productId);
    const cards = ids
      .map((pid) => db.products.find((p) => p.id === pid))
      .filter((p): p is Product => Boolean(p))
      .map(toCard);
    return ok(cards);
  },

  async isFavorited(userId: string, productId: string): Promise<ApiResponse<boolean>> {
    await delay(40);
    return ok(db.favorites.some((f) => f.userId === userId && f.productId === productId));
  },

  async toggleFavorite(userId: string, productId: string): Promise<ApiResponse<{ favorited: boolean }>> {
    await delay(80);
    const product = db.products.find((p) => p.id === productId);
    if (!product) return fail(404, '商品不存在');
    const existing = db.favorites.find((f) => f.userId === userId && f.productId === productId);
    let favorited: boolean;
    if (existing) {
      db.favorites.splice(db.favorites.indexOf(existing), 1);
      product.favoriteCount = Math.max(0, product.favoriteCount - 1);
      favorited = false;
    } else {
      db.favorites.push({ id: genId('f'), userId, productId, createdAt: Date.now() });
      product.favoriteCount += 1;
      favorited = true;
    }
    db.persist();
    return ok({ favorited });
  },

  // ======================= 会话 / 消息 =======================
  async listSessions(userId: string): Promise<ApiResponse<SessionView[]>> {
    await delay();
    const views = db.sessions
      .filter((s) => s.buyerId === userId || s.sellerId === userId)
      .sort((a, b) => b.lastMessageTime - a.lastMessageTime)
      .map((s) => this._sessionView(s, userId));
    return ok(views);
  },

  async startSession(productId: string, buyerId: string): Promise<ApiResponse<SessionView>> {
    await delay();
    const product = db.products.find((p) => p.id === productId);
    if (!product) return fail(404, '商品不存在');
    if (product.sellerId === buyerId) return fail(400, '不能和自己交易');
    let session = db.sessions.find((s) => s.productId === productId && s.buyerId === buyerId);
    if (!session) {
      session = {
        id: genId('s'),
        productId,
        buyerId,
        sellerId: product.sellerId,
        lastMessage: '',
        lastMessageTime: Date.now(),
        status: 'ACTIVE',
        buyerUnread: 0,
        sellerUnread: 0,
      };
      db.sessions.push(session);
      db.persist();
    }
    return ok(this._sessionView(session, buyerId));
  },

  async getThread(sessionId: string, userId: string): Promise<ApiResponse<ChatThread>> {
    await delay();
    const session = db.sessions.find((s) => s.id === sessionId);
    if (!session) return fail(404, '会话不存在');
    // 读取即清零自己的未读
    if (session.buyerId === userId) session.buyerUnread = 0;
    if (session.sellerId === userId) session.sellerUnread = 0;
    db.persist();
    const messages = db.messages.filter((m) => m.sessionId === sessionId).sort((a, b) => a.createdAt - b.createdAt);
    return ok({ session: this._sessionView(session, userId), messages });
  },

  async sendMessage(
    sessionId: string,
    senderId: string,
    payload: { type: MessageType; content: string; appointment?: Appointment },
  ): Promise<ApiResponse<Message>> {
    await delay(90);
    const session = db.sessions.find((s) => s.id === sessionId);
    if (!session) return fail(404, '会话不存在');
    const now = Date.now();
    const message: Message = {
      id: genId('m'),
      sessionId,
      senderId,
      type: payload.type,
      content: payload.content,
      appointment: payload.appointment,
      createdAt: now,
    };
    db.messages.push(message);
    session.lastMessage =
      payload.type === 'APPOINTMENT'
        ? `[约定] ${payload.appointment?.place ?? ''} ${payload.appointment?.time ?? ''}`
        : payload.type === 'IMAGE'
          ? '[图片]'
          : payload.content;
    session.lastMessageTime = now;
    // 给对方加未读
    if (session.buyerId === senderId) session.sellerUnread += 1;
    else session.buyerUnread += 1;
    db.persist();
    return ok(message);
  },

  async respondAppointment(messageId: string, status: AppointmentStatus): Promise<ApiResponse<Message>> {
    await delay(70);
    const msg = db.messages.find((m) => m.id === messageId);
    if (!msg || !msg.appointment) return fail(404, '约定不存在');
    msg.appointment.status = status;
    db.persist();
    return ok(msg);
  },

  // 卖家在聊天里标记已售出：商品 -> SOLD，会话 -> COMPLETED
  async markSoldInSession(sessionId: string, sellerId: string): Promise<ApiResponse<SessionView>> {
    await delay();
    const session = db.sessions.find((s) => s.id === sessionId);
    if (!session) return fail(404, '会话不存在');
    if (session.sellerId !== sellerId) return fail(403, '只有卖家可以标记成交');
    const product = db.products.find((p) => p.id === session.productId);
    if (!product) return fail(404, '商品不存在');
    if (product.status !== 'SOLD') {
      try {
        product.status = transition(product.status, 'MARK_SOLD');
        product.updatedAt = Date.now();
      } catch (e) {
        return fail(400, (e as Error).message);
      }
    }
    session.status = 'COMPLETED';
    db.persist();
    return ok(this._sessionView(session, sellerId));
  },

  // ======================= 举报 =======================
  async submitReport(payload: {
    reporterId: string;
    targetType: ReportTargetType;
    targetId: string;
    reason: ReportReason;
    description?: string;
  }): Promise<ApiResponse<Report>> {
    await delay();
    const report: Report = {
      id: genId('r'),
      reporterId: payload.reporterId,
      targetType: payload.targetType,
      targetId: payload.targetId,
      reason: payload.reason,
      description: payload.description,
      status: 'PENDING',
      createdAt: Date.now(),
    };
    db.reports.push(report);
    db.persist();
    return ok(report);
  },

  // ======================= AI =======================
  async estimatePrice(input: Parameters<typeof estimatePrice>[0]): Promise<ApiResponse<PriceEstimate>> {
    await delay(450); // 故意慢一点，体现"AI 正在估价"
    return ok(estimatePrice(input));
  },

  async generateDescription(input: Parameters<typeof generateDescription>[0]): Promise<ApiResponse<string>> {
    await delay(500);
    return ok(generateDescription(input));
  },

  // ======================= 后台管理 =======================
  async adminStats(): Promise<ApiResponse<AdminStats>> {
    await delay();
    const startOfToday = new Date();
    startOfToday.setHours(0, 0, 0, 0);
    const t0 = startOfToday.getTime();
    const stats: AdminStats = {
      userCount: db.users.filter((u) => u.role === 'STUDENT').length,
      verifiedCount: db.users.filter((u) => u.verifyStatus === 'VERIFIED' && u.role === 'STUDENT').length,
      productCount: db.products.length,
      todayNewProducts: db.products.filter((p) => p.createdAt >= t0).length,
      pendingVerifications: db.verifications.filter((v) => v.status === 'PENDING').length,
      pendingReports: db.reports.filter((r) => r.status === 'PENDING').length,
      completedDeals: db.products.filter((p) => p.status === 'SOLD').length,
    };
    return ok(stats);
  },

  async adminListUsers(keyword?: string): Promise<ApiResponse<User[]>> {
    await delay();
    let users = db.users.filter((u) => u.role === 'STUDENT');
    if (keyword) {
      const kw = keyword.trim().toLowerCase();
      users = users.filter((u) => (u.nickname + (u.studentNo ?? '')).toLowerCase().includes(kw));
    }
    return ok([...users].sort((a, b) => b.createdAt - a.createdAt));
  },

  async adminSetUserStatus(userId: string, status: UserStatus): Promise<ApiResponse<User>> {
    await delay();
    const u = userById(userId);
    if (!u) return fail(404, '用户不存在');
    u.status = status;
    db.persist();
    return ok(u);
  },

  async adminListProducts(keyword?: string, status?: Product['status']): Promise<ApiResponse<ProductCard[]>> {
    await delay();
    let items = [...db.products];
    if (status) items = items.filter((p) => p.status === status);
    if (keyword) {
      const kw = keyword.trim().toLowerCase();
      items = items.filter((p) => p.title.toLowerCase().includes(kw));
    }
    items.sort((a, b) => b.createdAt - a.createdAt);
    return ok(items.map(toCard));
  },

  async adminDelistProduct(id: string): Promise<ApiResponse<ProductCard>> {
    await delay();
    const p = db.products.find((x) => x.id === id);
    if (!p) return fail(404, '商品不存在');
    if (p.status !== 'SOLD') p.status = 'DELISTED';
    p.updatedAt = Date.now();
    db.persist();
    return ok(toCard(p));
  },

  async adminDeleteProduct(id: string): Promise<ApiResponse<{ id: string }>> {
    await delay();
    const i = db.products.findIndex((x) => x.id === id);
    if (i < 0) return fail(404, '商品不存在');
    db.products.splice(i, 1);
    db.persist();
    return ok({ id });
  },

  async adminListVerifications(): Promise<ApiResponse<VerificationView[]>> {
    await delay();
    const views = [...db.verifications]
      .sort((a, b) => Number(a.status !== 'PENDING') - Number(b.status !== 'PENDING') || b.createdAt - a.createdAt)
      .map((v) => ({ ...v, user: brief(userById(v.userId)) }));
    return ok(views);
  },

  async adminListReports(): Promise<ApiResponse<ReportView[]>> {
    await delay();
    const views = [...db.reports]
      .sort((a, b) => Number(a.status !== 'PENDING') - Number(b.status !== 'PENDING') || b.createdAt - a.createdAt)
      .map((r) => {
        let targetTitle = '未知对象';
        if (r.targetType === 'PRODUCT') targetTitle = db.products.find((p) => p.id === r.targetId)?.title ?? '已删除商品';
        else targetTitle = userById(r.targetId)?.nickname ?? '已注销用户';
        return { ...r, reporter: brief(userById(r.reporterId)), targetTitle };
      });
    return ok(views);
  },

  async adminHandleReport(id: string, resolution: ReportStatus, delistTarget = false): Promise<ApiResponse<Report>> {
    await delay();
    const r = db.reports.find((x) => x.id === id);
    if (!r) return fail(404, '举报不存在');
    r.status = resolution;
    r.handledAt = Date.now();
    if (resolution === 'RESOLVED' && delistTarget && r.targetType === 'PRODUCT') {
      const p = db.products.find((x) => x.id === r.targetId);
      if (p && p.status !== 'SOLD') {
        p.status = 'DELISTED';
        p.updatedAt = Date.now();
      }
    }
    db.persist();
    return ok(r);
  },

  // ---------- 私有 ----------
  _sessionView(s: import('../domain/types').ChatSession, viewerId: string): SessionView {
    const product = db.products.find((p) => p.id === s.productId);
    const counterpartId = s.buyerId === viewerId ? s.sellerId : s.buyerId;
    const myUnread = s.buyerId === viewerId ? s.buyerUnread : s.sellerUnread;
    return {
      ...s,
      product: product ? toCard(product) : null,
      counterpart: brief(userById(counterpartId)),
      myUnread,
    };
  },
};

export type ApiClient = typeof api;
