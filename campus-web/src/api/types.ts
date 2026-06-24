// API 信封与视图模型。信封与后端 campus-common ApiResponse 完全一致。
import type {
  Product,
  ChatSession,
  Message,
  CampusId,
  VerifyStatus,
  ConditionLevel,
} from '../domain/types';

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export function ok<T>(data: T): ApiResponse<T> {
  return { code: 200, message: 'success', data };
}

export function fail<T = null>(code: number, message: string): ApiResponse<T> {
  return { code, message, data: null as T };
}

export function isOk<T>(res: ApiResponse<T>): boolean {
  return res.code === 200;
}

// 卖家/用户摘要（列表与详情里内嵌，避免前端再查一次用户）
export interface UserBrief {
  id: string;
  nickname: string;
  avatar: string;
  rating: number;
  verifyStatus: VerifyStatus;
  campus: CampusId;
}

// 商品卡（列表用）：实体 + 卖家摘要
export interface ProductCard extends Product {
  seller: UserBrief;
}

// 商品详情：目前与卡片同形，预留扩展位
export type ProductDetail = ProductCard;

// 会话视图（消息列表/聊天页用）
export interface SessionView extends ChatSession {
  product: ProductCard | null;
  counterpart: UserBrief; // 对方（相对于当前用户）
  myUnread: number;
}

export interface ChatThread {
  session: SessionView;
  messages: Message[];
}

// 商品查询条件
export type ProductSort = 'NEWEST' | 'NEAREST' | 'PRICE_ASC' | 'HOT';

export interface ProductQuery {
  campus?: CampusId; // 不传表示全部校区
  categoryId?: string;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  condition?: ConditionLevel;
  sort?: ProductSort;
  viewerCampus?: CampusId; // 用于"距离最近"排序
}
