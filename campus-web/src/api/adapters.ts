// 后端 DTO ↔ 前端视图模型映射。后端 DTO 很精简，缺的字段在此优雅降级。
import type { User, ProductStatus, ConditionLevel, CampusId } from '../domain/types';
import type { ProductCard, UserBrief } from './types';

// ---------- 后端 DTO 形状（对应 backend/m2-gaps 的 record） ----------
export interface BackendUserProfile {
  id: number;
  username: string;
  nickname: string;
}

export interface BackendLoginResponse {
  token: string;
  user: BackendUserProfile;
}

export type BackendProductStatus = 'ON_SALE' | 'OFF_SALE' | 'SOLD';

export interface BackendProduct {
  id: number;
  sellerId: number;
  title: string;
  description: string;
  category: string;
  price: number;
  status: BackendProductStatus;
  favoriteCount?: number;
}

// ---------- 状态映射 ----------
const STATUS_FROM_BACKEND: Record<BackendProductStatus, ProductStatus> = {
  ON_SALE: 'ON_SALE',
  OFF_SALE: 'DELISTED',
  SOLD: 'SOLD',
};

// 前端状态机动作 → 后端三态（后端无"交易中"，归一为在售）
export function actionToBackendStatus(action: string): BackendProductStatus {
  switch (action) {
    case 'MARK_SOLD':
      return 'SOLD';
    case 'DELIST':
      return 'OFF_SALE';
    default:
      return 'ON_SALE'; // RELIST / CANCEL / RESERVE 等
  }
}

// 后端 category 是分类名字符串；映射到本地 categoryId 以显示正确图标。
// 名字与 data/seed.ts 的分类保持一致；无匹配时保留原值（图标回退 📦）。
const CATEGORY_NAME_TO_ID: Record<string, string> = {
  数码: 'c1',
  图书教材: 'c2',
  生活用品: 'c3',
  运动户外: 'c4',
  服饰鞋包: 'c5',
  美妆个护: 'c6',
  票券卡券: 'c7',
  其他闲置: 'c8',
};

function toCategoryId(category: string): string {
  return CATEGORY_NAME_TO_ID[category] ?? category;
}

// ---------- 用户 ----------
// 后端 UserProfileResponse 只有 id/username/nickname，其余给安全默认值：
// verifyStatus 默认 VERIFIED 以放行发布/聊天（后端无认证概念）。
export function adaptUser(p: BackendUserProfile): User {
  return {
    id: String(p.id),
    nickname: p.nickname || p.username,
    avatar: '🙂',
    email: p.username,
    school: '',
    campus: 'SOUTH',
    verifyStatus: 'VERIFIED',
    role: 'STUDENT',
    status: 'ACTIVE',
    rating: 5,
    createdAt: Date.now(),
  };
}

function adaptSellerBrief(sellerId: number): UserBrief {
  return {
    id: String(sellerId),
    nickname: `用户 ${sellerId}`,
    avatar: '🙂',
    rating: 5,
    verifyStatus: 'VERIFIED',
    campus: 'SOUTH',
  };
}

// ---------- 商品 ----------
// 后端 ProductResponse 缺成色/校区/图片/计数等，给默认值，保证前端组件正常渲染。
export function adaptProduct(p: BackendProduct): ProductCard {
  const now = Date.now();
  return {
    id: String(p.id),
    sellerId: String(p.sellerId),
    title: p.title,
    description: p.description,
    categoryId: toCategoryId(p.category),
    price: Number(p.price),
    originalPrice: undefined,
    conditionLevel: 'GOOD' as ConditionLevel,
    campus: 'SOUTH' as CampusId,
    locationDesc: '',
    negotiable: true,
    status: STATUS_FROM_BACKEND[p.status] ?? 'ON_SALE',
    images: [{ url: '', sortOrder: 0 }],
    viewCount: 0,
    favoriteCount: p.favoriteCount ?? 0,
    createdAt: now,
    updatedAt: now,
    seller: adaptSellerBrief(p.sellerId),
  };
}
