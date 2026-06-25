// 商品状态机：唯一真相来源。
// 任何状态变更都必须经过 canTransition() / transition()，页面不允许直接给 status 赋值。
import type { ProductStatus } from './types';

// 卖家/系统可触发的动作
export type ProductAction =
  | 'PUBLISH' // 提交发布
  | 'APPROVE' // 审核通过
  | 'REJECT' // 审核驳回
  | 'RESUBMIT' // 驳回后重新提交
  | 'RESERVE' // 进入交易中（买家确认/卖家锁定）
  | 'CANCEL' // 取消交易，回到在售
  | 'MARK_SOLD' // 标记已售出
  | 'DELIST' // 下架
  | 'RELIST'; // 重新上架

// 允许的迁移：当前状态 -> 动作 -> 目标状态
const TRANSITIONS: Partial<Record<ProductStatus, Partial<Record<ProductAction, ProductStatus>>>> = {
  REVIEWING: {
    APPROVE: 'ON_SALE',
    REJECT: 'REJECTED',
  },
  REJECTED: {
    RESUBMIT: 'REVIEWING',
  },
  ON_SALE: {
    RESERVE: 'RESERVED',
    MARK_SOLD: 'SOLD',
    DELIST: 'DELISTED',
  },
  RESERVED: {
    CANCEL: 'ON_SALE',
    MARK_SOLD: 'SOLD',
    DELIST: 'DELISTED',
  },
  DELISTED: {
    RELIST: 'ON_SALE',
  },
  // SOLD 是终态，无出边
};

export function canTransition(from: ProductStatus, action: ProductAction): boolean {
  return Boolean(TRANSITIONS[from]?.[action]);
}

export function nextStatus(from: ProductStatus, action: ProductAction): ProductStatus | null {
  return TRANSITIONS[from]?.[action] ?? null;
}

/**
 * 执行状态迁移。非法迁移抛错（让 bug 早暴露，而不是悄悄写坏数据）。
 */
export function transition(from: ProductStatus, action: ProductAction): ProductStatus {
  const to = nextStatus(from, action);
  if (!to) {
    throw new Error(`非法的商品状态迁移：${from} 不能执行 ${action}`);
  }
  return to;
}

// 在售中（对买家可见、可交易）
export function isBuyable(status: ProductStatus): boolean {
  return status === 'ON_SALE';
}

// 终态：不再参与流转
export function isTerminal(status: ProductStatus): boolean {
  return status === 'SOLD';
}

// 卖家在"我的发布"里可执行的动作（用于渲染操作按钮）
export function sellerActions(status: ProductStatus): ProductAction[] {
  switch (status) {
    case 'ON_SALE':
      return ['MARK_SOLD', 'DELIST'];
    case 'RESERVED':
      return ['MARK_SOLD', 'CANCEL', 'DELIST'];
    case 'DELISTED':
      return ['RELIST'];
    case 'REJECTED':
      return ['RESUBMIT'];
    default:
      return [];
  }
}

export const ACTION_LABEL: Record<ProductAction, string> = {
  PUBLISH: '发布',
  APPROVE: '通过审核',
  REJECT: '驳回',
  RESUBMIT: '重新提交',
  RESERVE: '锁定交易',
  CANCEL: '取消交易',
  MARK_SOLD: '标记已售出',
  DELIST: '下架',
  RELIST: '重新上架',
};
