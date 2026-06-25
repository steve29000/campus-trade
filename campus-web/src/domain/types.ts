// 领域类型：实体 + 状态联合类型。纯 TS，无 UI 依赖。
// 注意：tsconfig 开启了 erasableSyntaxOnly，因此不用 enum，改用字符串字面量联合 + const 标签表。

// ---------- 角色与用户状态 ----------
export type UserRole = 'STUDENT' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'BANNED';

// 校园认证状态：未认证 → 审核中 → 已认证 / 失败
export type VerifyStatus = 'UNVERIFIED' | 'PENDING' | 'VERIFIED' | 'REJECTED';

export const VERIFY_STATUS_LABEL: Record<VerifyStatus, string> = {
  UNVERIFIED: '未认证',
  PENDING: '审核中',
  VERIFIED: '已认证',
  REJECTED: '认证失败',
};

export interface User {
  id: string;
  nickname: string;
  avatar: string;
  phone?: string;
  email?: string;
  school: string;
  campus: CampusId;
  studentNo?: string;
  verifyStatus: VerifyStatus;
  role: UserRole;
  status: UserStatus;
  rating: number; // 信用分 0-5
  createdAt: number;
}

// ---------- 校区 ----------
export type CampusId = 'SOUTH' | 'NORTH';

// ---------- 分类 ----------
export interface Category {
  id: string;
  name: string;
  icon: string; // emoji，MVP 不引入图标库
  sortOrder: number;
  hot?: boolean; // 是否处于需求旺季（影响 AI 估价热门系数）
}

// ---------- 成色 ----------
export type ConditionLevel = 'NEW' | 'ALMOST_NEW' | 'GOOD' | 'WORN';

export const CONDITION_LABEL: Record<ConditionLevel, string> = {
  NEW: '全新',
  ALMOST_NEW: '几乎全新',
  GOOD: '成色良好',
  WORN: '有明显使用痕迹',
};

// ---------- 商品状态 ----------
// MVP 第一版使用 ON_SALE / RESERVED / SOLD / DELISTED；
// REVIEWING / REJECTED 在开启后台审核时启用。
export type ProductStatus =
  | 'REVIEWING'
  | 'ON_SALE'
  | 'RESERVED'
  | 'SOLD'
  | 'DELISTED'
  | 'REJECTED';

export const PRODUCT_STATUS_LABEL: Record<ProductStatus, string> = {
  REVIEWING: '审核中',
  ON_SALE: '在售',
  RESERVED: '交易中',
  SOLD: '已售出',
  DELISTED: '已下架',
  REJECTED: '审核失败',
};

export interface ProductImage {
  url: string;
  sortOrder: number;
}

export interface Product {
  id: string;
  sellerId: string;
  title: string;
  description: string;
  categoryId: string;
  price: number;
  originalPrice?: number;
  conditionLevel: ConditionLevel;
  campus: CampusId;
  locationDesc: string; // 交易地点建议，如"南校区图书馆门口"
  negotiable: boolean; // 是否可议价
  status: ProductStatus;
  images: ProductImage[];
  viewCount: number;
  favoriteCount: number;
  createdAt: number;
  updatedAt: number;
}

// ---------- 收藏 ----------
export interface Favorite {
  id: string;
  userId: string;
  productId: string;
  createdAt: number;
}

// ---------- 聊天会话 ----------
export type SessionStatus = 'ACTIVE' | 'COMPLETED' | 'CLOSED';

export interface ChatSession {
  id: string;
  productId: string;
  buyerId: string;
  sellerId: string;
  lastMessage: string;
  lastMessageTime: number;
  status: SessionStatus;
  buyerUnread: number;
  sellerUnread: number;
}

// ---------- 消息 ----------
export type MessageType = 'TEXT' | 'IMAGE' | 'APPOINTMENT';
export type AppointmentStatus = 'PROPOSED' | 'CONFIRMED' | 'CANCELLED';

export interface Appointment {
  place: string;
  time: string; // 展示用文本，如"今天 18:30"
  status: AppointmentStatus;
}

export interface Message {
  id: string;
  sessionId: string;
  senderId: string;
  type: MessageType;
  content: string; // TEXT/IMAGE 用；APPOINTMENT 用 appointment 字段
  appointment?: Appointment;
  createdAt: number;
}

// ---------- 校园认证申请 ----------
export interface Verification {
  id: string;
  userId: string;
  school: string;
  campus: CampusId;
  studentNo: string;
  proofImage?: string;
  status: VerifyStatus;
  rejectReason?: string;
  createdAt: number;
  reviewedAt?: number;
}

// ---------- 举报 ----------
export type ReportTargetType = 'PRODUCT' | 'USER';
export type ReportStatus = 'PENDING' | 'RESOLVED' | 'DISMISSED';

export const REPORT_REASONS = [
  '虚假商品',
  '价格欺诈',
  '卖家失联',
  '违规内容',
  '已售但未下架',
  '其他',
] as const;
export type ReportReason = (typeof REPORT_REASONS)[number];

export interface Report {
  id: string;
  reporterId: string;
  targetType: ReportTargetType;
  targetId: string;
  reason: ReportReason;
  description?: string;
  status: ReportStatus;
  createdAt: number;
  handledAt?: number;
}

// ---------- 订单（线下面交，无在线支付） ----------
export type OrderStatus = 'ONGOING' | 'COMPLETED' | 'CANCELLED';

export interface Order {
  id: string;
  productId: string;
  buyerId: string;
  sellerId: string;
  status: OrderStatus;
  place?: string;
  createdAt: number;
  completedAt?: number;
}

// ---------- AI 估价记录 ----------
export interface AIPriceRecord {
  id: string;
  userId: string;
  categoryId: string;
  conditionLevel: ConditionLevel;
  originalPrice: number;
  suggestedMin: number;
  suggestedMax: number;
  suggestedPrice: number;
  reason: string;
  createdAt: number;
}
