// 聊天领域常量与小工具。
import type { SessionStatus } from './types';

// 校园二手很适合做快捷短语
export const QUICK_PHRASES = [
  '还在吗？',
  '可以便宜一点吗？',
  '什么时候方便交易？',
  '可以约个时间面交吗？',
  '能再发张细节图吗？',
];

// 约交易时间预设
export const APPOINTMENT_TIME_PRESETS = ['今天 12:30', '今天 18:30', '明天 12:30', '明天 18:00', '周末上午'];

// 会话是否已结束（成交/关闭）
export const SESSION_STATUS_DONE: SessionStatus[] = ['COMPLETED', 'CLOSED'];
