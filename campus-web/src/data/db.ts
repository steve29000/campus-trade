// 内存数据库 + localStorage 持久化。api 层通过它读写，将来可整体替换为真实后端。
import { buildSeed, type SeedData } from './seed';

const STORAGE_KEY = 'campus-web-db-v1';

function load(): SeedData {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw) as SeedData;
  } catch {
    // 解析失败则回退到种子数据
  }
  const fresh = buildSeed(Date.now());
  persistRaw(fresh);
  return fresh;
}

function persistRaw(data: SeedData) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } catch {
    // 存储不可用时忽略，内存态仍可用
  }
}

const state: SeedData = load();

let idSeq = Date.now();
export function genId(prefix: string): string {
  idSeq += 1;
  return `${prefix}_${idSeq.toString(36)}`;
}

export const db = {
  get users() {
    return state.users;
  },
  get categories() {
    return state.categories;
  },
  get products() {
    return state.products;
  },
  get favorites() {
    return state.favorites;
  },
  get sessions() {
    return state.sessions;
  },
  get messages() {
    return state.messages;
  },
  get verifications() {
    return state.verifications;
  },
  get reports() {
    return state.reports;
  },
  persist() {
    persistRaw(state);
  },
  /** 清空并重新播种（调试用） */
  reset() {
    const fresh = buildSeed(Date.now());
    state.users = fresh.users;
    state.categories = fresh.categories;
    state.products = fresh.products;
    state.favorites = fresh.favorites;
    state.sessions = fresh.sessions;
    state.messages = fresh.messages;
    state.verifications = fresh.verifications;
    state.reports = fresh.reports;
    persistRaw(state);
  },
};
