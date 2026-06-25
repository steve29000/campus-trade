// AI 能力第一版：规则模拟，不接真实模型。保留接口形状，将来替换为 campus-ai /ai/* 。
import type { ConditionLevel, CampusId } from './types';
import { CONDITION_LABEL } from './types';
import { campusName } from './campus';

// 成色系数
const CONDITION_FACTOR: Record<ConditionLevel, number> = {
  NEW: 0.8,
  ALMOST_NEW: 0.65,
  GOOD: 0.5,
  WORN: 0.3,
};

export interface PriceEstimateInput {
  originalPrice: number;
  conditionLevel: ConditionLevel;
  categoryName: string;
  categoryHot?: boolean; // 是否处于需求旺季
}

export interface PriceEstimate {
  suggestedPrice: number;
  suggestedMin: number;
  suggestedMax: number;
  reason: string;
}

/**
 * 推荐价 = 原价 × 成色系数 × 热门系数
 * 建议区间 = [推荐价 × 0.85, 推荐价 × 1.15]
 */
export function estimatePrice(input: PriceEstimateInput): PriceEstimate {
  const { originalPrice, conditionLevel, categoryName, categoryHot } = input;
  const conditionFactor = CONDITION_FACTOR[conditionLevel];
  const hotFactor = categoryHot ? 1.1 : 1.0;

  const raw = originalPrice * conditionFactor * hotFactor;
  const suggestedPrice = roundToNiceYuan(raw);
  const suggestedMin = roundToNiceYuan(suggestedPrice * 0.85);
  const suggestedMax = roundToNiceYuan(suggestedPrice * 1.15);

  const reasons: string[] = [
    `同类「${categoryName}」在本校区常见成交价区间为 ¥${suggestedMin} - ¥${suggestedMax}`,
    `当前成色为「${CONDITION_LABEL[conditionLevel]}」`,
  ];
  if (categoryHot) reasons.push('临近开学季，该类目需求上升');

  return {
    suggestedPrice,
    suggestedMin,
    suggestedMax,
    reason: reasons.join('；') + '。',
  };
}

// 取整到更"顺眼"的价格：<100 取整到 5 的倍数，否则取整到 10 的倍数
function roundToNiceYuan(v: number): number {
  if (v <= 0) return 0;
  const step = v < 100 ? 5 : 10;
  return Math.max(step, Math.round(v / step) * step);
}

export interface DescriptionInput {
  title: string;
  rawNotes: string; // 用户随手写的，如"有机化学教材，八成新，有笔记"
  conditionLevel: ConditionLevel;
  categoryName: string;
  campus: CampusId;
  tradePlace?: string;
}

/**
 * AI 文案：把零散信息扩写成一段完整、可读、贴合校园交易场景的描述。
 */
export function generateDescription(input: DescriptionInput): string {
  const { title, rawNotes, conditionLevel, categoryName, campus, tradePlace } = input;
  const place = tradePlace || `${campusName(campus)}图书馆附近`;
  const conditionText = CONDITION_LABEL[conditionLevel];

  const notes = rawNotes.trim() || `${title}，${conditionText}`;
  const parts = [
    `【${title}】${notes}。`,
    `整体${conditionText}，功能/品相良好，不影响正常使用。`,
    `适合${categoryName}相关需求的同学，价格可小刀，诚心可议。`,
    `可在${place}当面交易，看货满意再成交，安全放心。`,
  ];
  return parts.join('');
}
