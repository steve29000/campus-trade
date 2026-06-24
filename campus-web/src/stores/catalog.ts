import { defineStore } from 'pinia';
import { ref } from 'vue';
import { api } from '../api/client';
import { isOk } from '../api/types';
import type { Category } from '../domain/types';

export const useCatalogStore = defineStore('catalog', () => {
  const categories = ref<Category[]>([]);
  const loaded = ref(false);

  async function load() {
    if (loaded.value) return;
    const res = await api.listCategories();
    if (isOk(res)) {
      categories.value = res.data;
      loaded.value = true;
    }
  }

  function categoryName(id: string): string {
    return categories.value.find((c) => c.id === id)?.name ?? '其他';
  }

  function categoryIcon(id: string): string {
    return categories.value.find((c) => c.id === id)?.icon ?? '📦';
  }

  return { categories, loaded, load, categoryName, categoryIcon };
});
