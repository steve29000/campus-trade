// HTTP 实现：只覆盖后端已有的域（用户 + 商品）。其余方法在 client.ts 里自动回退 mock。
import { http } from './http';
import { setToken } from './token';
import { ok, fail, isOk, type ApiResponse, type ProductCard, type ProductDetail, type ProductQuery } from './types';
import type { User, CampusId, ConditionLevel } from '../domain/types';
import type { ProductAction } from '../domain/productStatus';
import {
  adaptUser,
  adaptProduct,
  actionToBackendStatus,
  type BackendUserProfile,
  type BackendLoginResponse,
  type BackendProduct,
} from './adapters';

interface RegisterPayload {
  nickname: string;
  email: string;
  school: string;
  campus: CampusId;
  username?: string;
  password?: string;
}

interface CreatePayload {
  sellerId: string;
  title: string;
  description: string;
  categoryId: string;
  price: number;
  originalPrice?: number;
  conditionLevel: ConditionLevel;
  campus: CampusId;
  locationDesc: string;
  negotiable: boolean;
  imageCount: number;
}

// 后端 ProductResponse 只给 sellerId，这里按需查 /user/{id} 补真实昵称（去重 + 进程内缓存）。
const sellerNameCache = new Map<string, string>();

async function enrichSellers(cards: ProductCard[]): Promise<void> {
  const need = [...new Set(cards.map((c) => c.sellerId))].filter((id) => !sellerNameCache.has(id));
  await Promise.all(
    need.map(async (id) => {
      const r = await http.get<BackendUserProfile>(`/user/${id}`);
      if (isOk(r) && r.data) sellerNameCache.set(id, r.data.nickname || r.data.username);
    }),
  );
  for (const c of cards) {
    const name = sellerNameCache.get(c.sellerId);
    if (name) c.seller.nickname = name;
  }
}

export const httpApi = {
  async login(emailOrId: string, password?: string): Promise<ApiResponse<User>> {
    const res = await http.post<BackendLoginResponse>('/user/login', { username: emailOrId, password: password ?? '' });
    if (!isOk(res) || !res.data) return fail(res.code || 401, res.message || '登录失败');
    setToken(res.data.token);
    return ok(adaptUser(res.data.user));
  },

  async register(payload: RegisterPayload): Promise<ApiResponse<User>> {
    const username = (payload.username || payload.email || '').trim();
    const password = payload.password ?? '';
    const reg = await http.post<BackendUserProfile>('/user/register', {
      username,
      password,
      nickname: payload.nickname,
    });
    if (!isOk(reg)) return fail(reg.code || 400, reg.message || '注册失败');
    // 注册接口不返回 token，自动登录拿令牌
    return httpApi.login(username, password);
  },

  async me(userId: string): Promise<ApiResponse<User>> {
    const res = await http.get<BackendUserProfile>(`/user/${userId}`);
    if (!isOk(res) || !res.data) return fail(res.code || 404, res.message || '用户不存在');
    return ok(adaptUser(res.data));
  },

  async listProducts(query: ProductQuery = {}): Promise<ApiResponse<ProductCard[]>> {
    const params = new URLSearchParams();
    if (query.keyword) params.set('keyword', query.keyword);
    if (query.categoryId) params.set('category', query.categoryId);
    const qs = params.toString();
    const res = await http.get<BackendProduct[]>(`/product${qs ? `?${qs}` : ''}`);
    if (!isOk(res) || !res.data) return fail(res.code, res.message);
    const cards = res.data.map(adaptProduct);
    await enrichSellers(cards);
    return ok(cards);
  },

  async getProduct(id: string): Promise<ApiResponse<ProductDetail>> {
    const res = await http.get<BackendProduct>(`/product/${id}`);
    if (!isOk(res) || !res.data) return fail(res.code || 404, res.message || '商品不存在');
    const card = adaptProduct(res.data);
    await enrichSellers([card]);
    return ok(card);
  },

  async myProducts(sellerId: string): Promise<ApiResponse<ProductCard[]>> {
    // 后端无"我的发布"接口，取全部后按 sellerId 过滤
    const res = await http.get<BackendProduct[]>('/product');
    if (!isOk(res) || !res.data) return fail(res.code, res.message);
    const cards = res.data.filter((p) => String(p.sellerId) === sellerId).map(adaptProduct);
    await enrichSellers(cards);
    return ok(cards);
  },

  async createProduct(payload: CreatePayload): Promise<ApiResponse<ProductCard>> {
    const res = await http.post<BackendProduct>('/product', {
      sellerId: Number(payload.sellerId),
      title: payload.title,
      description: payload.description,
      category: payload.categoryId,
      price: payload.price,
    });
    if (!isOk(res) || !res.data) return fail(res.code, res.message);
    return ok(adaptProduct(res.data));
  },

  async changeProductStatus(id: string, action: ProductAction, _actorId: string): Promise<ApiResponse<ProductCard>> {
    const res = await http.put<BackendProduct>(`/product/${id}/status`, { status: actionToBackendStatus(action) });
    if (!isOk(res) || !res.data) return fail(res.code, res.message);
    return ok(adaptProduct(res.data));
  },
};
