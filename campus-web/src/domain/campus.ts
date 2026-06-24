// 校园特色：校区、安全交易地点、距离。
import type { CampusId } from './types';

export interface Campus {
  id: CampusId;
  name: string;
}

export const CAMPUSES: Campus[] = [
  { id: 'SOUTH', name: '南校区' },
  { id: 'NORTH', name: '北校区' },
];

export function campusName(id: CampusId): string {
  return CAMPUSES.find((c) => c.id === id)?.name ?? '未知校区';
}

// 平台推荐的安全交易点（校园特色，比普通二手平台更贴合场景）。
export const TRADE_PLACES: Record<CampusId, string[]> = {
  SOUTH: ['南校区图书馆门口', '南校区食堂一楼', '南校区宿舍楼下', '南校区教学楼大厅'],
  NORTH: ['北校区图书馆门口', '北校区食堂大厅', '北校区宿舍区门口', '北校区体育馆门口'],
};

export function tradePlacesOf(campus: CampusId): string[] {
  return TRADE_PLACES[campus];
}

// 校园场景下"距离最近"比"销量最高"更重要。
// MVP 用确定性的伪距离（同校区更近、异校区更远）让排序/展示有意义，
// 真实实现可替换为地理坐标计算。
export function pseudoDistanceKm(viewerCampus: CampusId, product: { campus: CampusId; id: string }): number {
  const base = viewerCampus === product.campus ? 0.1 : 2.5;
  // 用 id 派生一个稳定的 0~0.9 抖动，避免每次渲染跳动
  const jitter = (hashString(product.id) % 90) / 100;
  return Math.round((base + jitter) * 10) / 10;
}

export function formatDistance(km: number): string {
  if (km < 1) return `${Math.round(km * 1000)} m`;
  return `${km.toFixed(1)} km`;
}

function hashString(s: string): number {
  let h = 0;
  for (let i = 0; i < s.length; i++) {
    h = (h * 31 + s.charCodeAt(i)) | 0;
  }
  return Math.abs(h);
}
