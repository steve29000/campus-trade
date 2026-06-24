import { defineStore } from 'pinia';
import { ref } from 'vue';
import { api } from '../api/client';
import { isOk } from '../api/types';
import { useAuthStore } from './auth';

// 收藏的商品 id 集合，跨页面共享，让"心形"即时一致。
export const useFavoriteStore = defineStore('favorites', () => {
  const ids = ref<Set<string>>(new Set());

  async function load() {
    const auth = useAuthStore();
    if (!auth.user) {
      ids.value = new Set();
      return;
    }
    const res = await api.listFavorites(auth.user.id);
    if (isOk(res)) ids.value = new Set(res.data.map((p) => p.id));
  }

  function has(productId: string): boolean {
    return ids.value.has(productId);
  }

  // 返回切换后是否已收藏；未登录返回 null（交给页面引导登录）
  async function toggle(productId: string): Promise<boolean | null> {
    const auth = useAuthStore();
    if (!auth.user) return null;
    const res = await api.toggleFavorite(auth.user.id, productId);
    if (!isOk(res)) return null;
    const next = new Set(ids.value);
    if (res.data.favorited) next.add(productId);
    else next.delete(productId);
    ids.value = next;
    return res.data.favorited;
  }

  function clear() {
    ids.value = new Set();
  }

  return { ids, load, has, toggle, clear };
});
