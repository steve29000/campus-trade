<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import ProductThumb from '../../components/ProductThumb.vue';
import { useCatalogStore } from '../../stores/catalog';
import { useToast } from '../../composables/toast';
import { api } from '../../api/client';
import { isOk, type ProductCard } from '../../api/types';
import { PRODUCT_STATUS_LABEL, type ProductStatus } from '../../domain/types';

const router = useRouter();
const catalog = useCatalogStore();
const toast = useToast();
const items = ref<ProductCard[]>([]);
const keyword = ref('');
const status = ref<ProductStatus | ''>('');
const loading = ref(true);

const statusOptions: { key: ProductStatus | ''; label: string }[] = [
  { key: '', label: '全部' },
  { key: 'ON_SALE', label: '在售' },
  { key: 'SOLD', label: '已售出' },
  { key: 'DELISTED', label: '已下架' },
];

async function load() {
  loading.value = true;
  const res = await api.adminListProducts(keyword.value || undefined, status.value || undefined);
  if (isOk(res)) items.value = res.data;
  loading.value = false;
}

async function delist(p: ProductCard) {
  const res = await api.adminDelistProduct(p.id);
  if (isOk(res)) {
    toast.show('已下架');
    await load();
  } else toast.show(res.message);
}

async function remove(p: ProductCard) {
  const res = await api.adminDeleteProduct(p.id);
  if (isOk(res)) {
    toast.show('已删除');
    await load();
  }
}

let t: ReturnType<typeof setTimeout>;
watch([keyword, status], () => {
  clearTimeout(t);
  t = setTimeout(load, 200);
});
onMounted(load);
</script>

<template>
  <div>
    <header class="topbar">
      <button class="topbar__back" @click="router.push('/admin')">‹</button>
      <span class="topbar__title">商品管理</span>
    </header>
    <div class="asearch">
      <input class="input" v-model="keyword" placeholder="搜索商品标题" />
      <div class="apfilters">
        <button
          v-for="o in statusOptions"
          :key="o.key"
          class="apfilter"
          :class="{ 'is-active': status === o.key }"
          @click="status = o.key"
        >
          {{ o.label }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="items.length === 0" class="empty"><div class="empty__emoji">📦</div><div class="empty__text">没有匹配的商品</div></div>
    <div v-else class="alist">
      <div v-for="p in items" :key="p.id" class="aprow">
        <div class="aprow__thumb" @click="router.push(`/product/${p.id}`)">
          <ProductThumb :seed="p.id" :emoji="catalog.categoryIcon(p.categoryId)" rounded />
        </div>
        <div class="aprow__main">
          <div class="aprow__title">{{ p.title }}</div>
          <div class="aprow__meta">
            <span class="price"><span class="price__symbol">¥</span>{{ p.price }}</span>
            <span class="tag">{{ PRODUCT_STATUS_LABEL[p.status] }}</span>
            <span class="faint" style="font-size:11px">{{ p.seller.nickname }}</span>
          </div>
          <div class="aprow__acts">
            <button v-if="p.status !== 'DELISTED' && p.status !== 'SOLD'" class="aprow__act" @click="delist(p)">下架</button>
            <button class="aprow__act is-danger" @click="remove(p)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.asearch {
  padding: 10px 14px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}
.apfilters {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.apfilter {
  border: none;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 5px 12px;
  border-radius: 999px;
}
.apfilter.is-active {
  background: var(--c-primary);
  color: #fff;
  font-weight: 600;
}
.alist {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.aprow {
  display: flex;
  gap: 12px;
  background: var(--c-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 10px;
}
.aprow__thumb {
  width: 70px;
  flex-shrink: 0;
}
.aprow__main {
  flex: 1;
  min-width: 0;
}
.aprow__title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.aprow__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 6px 0 8px;
  font-size: 15px;
}
.aprow__acts {
  display: flex;
  gap: 8px;
}
.aprow__act {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
}
.aprow__act.is-danger {
  border-color: #fecaca;
  color: var(--c-danger);
}
</style>
