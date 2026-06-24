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
    return ok(res.data.map(adaptProduct));
  },

  async getProduct(id: string): Promise<ApiResponse<ProductDetail>> {
    const res = await http.get<BackendProduct>(`/product/${id}`);
    if (!isOk(res) || !res.data) return fail(res.code || 404, res.message || '商品不存在');
    return ok(adaptProduct(res.data));
  },

  async myProducts(sellerId: string): Promise<ApiResponse<ProductCard[]>> {
    // 后端无"我的发布"接口，取全部后按 sellerId 过滤
    const res = await http.get<BackendProduct[]>('/product');
    if (!isOk(res) || !res.data) return fail(res.code, res.message);
    return ok(res.data.filter((p) => String(p.sellerId) === sellerId).map(adaptProduct));
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
