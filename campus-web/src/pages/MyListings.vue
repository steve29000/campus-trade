<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import ProductThumb from '../components/ProductThumb.vue';
import { useAuthStore } from '../stores/auth';
import { useCatalogStore } from '../stores/catalog';
import { useToast } from '../composables/toast';
import { api } from '../api/client';
import { isOk, type ProductCard } from '../api/types';
import { PRODUCT_STATUS_LABEL, type ProductStatus } from '../domain/types';
import { sellerActions, ACTION_LABEL, type ProductAction } from '../domain/productStatus';

const router = useRouter();
const auth = useAuthStore();
const catalog = useCatalogStore();
const toast = useToast();

const items = ref<ProductCard[]>([]);
const loading = ref(true);
const filter = ref<ProductStatus | 'ALL'>('ALL');

const filters: { key: ProductStatus | 'ALL'; label: string }[] = [
  { key: 'ALL', label: '全部' },
  { key: 'ON_SALE', label: '在售' },
  { key: 'RESERVED', label: '交易中' },
  { key: 'SOLD', label: '已售出' },
  { key: 'DELISTED', label: '已下架' },
];

const shown = computed(() =>
  filter.value === 'ALL' ? items.value : items.value.filter((p) => p.status === filter.value),
);

async function load() {
  if (!auth.user) return;
  loading.value = true;
  const res = await api.myProducts(auth.user.id);
  if (isOk(res)) items.value = res.data;
  loading.value = false;
}

async function act(p: ProductCard, action: ProductAction) {
  if (!auth.user) return;
  const res = await api.changeProductStatus(p.id, action, auth.user.id);
  if (isOk(res)) {
    toast.show(`已${ACTION_LABEL[action]}`);
    await load();
  } else {
    toast.show(res.message);
  }
}

// 危险操作样式
function actionClass(action: ProductAction): string {
  if (action === 'MARK_SOLD') return 'is-primary';
  if (action === 'DELIST') return 'is-danger';
  return '';
}

onMounted(load);
</script>

<template>
  <div>
    <header class="topbar">
      <button class="topbar__back" @click="router.back()">‹</button>
      <span class="topbar__title">我的发布</span>
      <button class="ml-pub" @click="router.push('/publish')">＋ 发布</button>
    </header>

    <div class="ml-filters">
      <button
        v-for="f in filters"
        :key="f.key"
        class="ml-filter"
        :class="{ 'is-active': filter === f.key }"
        @click="filter = f.key"
      >
        {{ f.label }}
      </button>
    </div>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="shown.length === 0" class="empty">
      <div class="empty__emoji">📦</div>
      <div class="empty__text">这里还没有商品</div>
      <button class="btn btn--primary" style="margin-top:14px" @click="router.push('/publish')">去发布闲置</button>
    </div>

    <div v-else class="ml-list">
      <div v-for="p in shown" :key="p.id" class="ml-card">
        <div class="ml-thumb" @click="router.push(`/product/${p.id}`)">
          <ProductThumb :seed="p.id" :emoji="catalog.categoryIcon(p.categoryId)" rounded />
        </div>
        <div class="ml-info">
          <div class="ml-title" @click="router.push(`/product/${p.id}`)">{{ p.title }}</div>
          <div class="ml-meta">
            <span class="price"><span class="price__symbol">¥</span>{{ p.price }}</span>
            <span class="ml-badge" :class="`s-${p.status}`">{{ PRODUCT_STATUS_LABEL[p.status] }}</span>
          </div>
          <div class="ml-actions">
            <button
              v-for="a in sellerActions(p.status)"
              :key="a"
              class="ml-act"
              :class="actionClass(a)"
              @click="act(p, a)"
            >
              {{ ACTION_LABEL[a] }}
            </button>
            <span v-if="sellerActions(p.status).length === 0" class="faint" style="font-size:12px">交易已完成</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ml-pub {
  margin-left: auto;
  border: none;
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
}
.ml-filters {
  display: flex;
  gap: 8px;
  padding: 10px 12px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
  overflow-x: auto;
}
.ml-filter {
  flex-shrink: 0;
  border: none;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 5px 12px;
  border-radius: 999px;
}
.ml-filter.is-active {
  background: var(--c-primary);
  color: #fff;
  font-weight: 600;
}
.ml-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ml-card {
  display: flex;
  gap: 12px;
  background: var(--c-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 10px;
}
.ml-thumb {
  width: 84px;
  flex-shrink: 0;
}
.ml-info {
  flex: 1;
  min-width: 0;
}
.ml-title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ml-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 6px 0 8px;
  font-size: 16px;
}
.ml-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
}
.ml-badge.s-ON_SALE {
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
}
.ml-badge.s-RESERVED {
  background: #fff7ed;
  color: #c2410c;
}
.ml-badge.s-SOLD {
  background: #f3f4f6;
  color: #6b7280;
}
.ml-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.ml-act {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
}
.ml-act.is-primary {
  border-color: var(--c-primary);
  background: var(--c-primary);
  color: #fff;
}
.ml-act.is-danger {
  border-color: #fecaca;
  color: var(--c-danger);
}

/* ---------- 桌面：居中限宽 ---------- */
@media (min-width: 900px) {
  .ml-filters,
  .ml-list {
    max-width: 760px;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
