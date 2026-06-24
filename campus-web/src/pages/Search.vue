<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import ProductCard from '../components/ProductCard.vue';
import { useAuthStore } from '../stores/auth';
import { useCatalogStore } from '../stores/catalog';
import { api } from '../api/client';
import { isOk, type ProductCard as Card, type ProductSort } from '../api/types';
import { CONDITION_LABEL, type ConditionLevel } from '../domain/types';

const route = useRoute();
const auth = useAuthStore();
const catalog = useCatalogStore();

const keyword = ref((route.query.kw as string) || '');
const categoryId = ref((route.query.cat as string) || '');
const sort = ref<ProductSort>('NEWEST');
const condition = ref<ConditionLevel | ''>('');
const minPrice = ref<number | null>(null);
const maxPrice = ref<number | null>(null);
const showFilter = ref(false);

const results = ref<Card[]>([]);
const loading = ref(false);

const sorts: { key: ProductSort; label: string }[] = [
  { key: 'NEWEST', label: '最新' },
  { key: 'NEAREST', label: '距离最近' },
  { key: 'PRICE_ASC', label: '价格最低' },
  { key: 'HOT', label: '人气' },
];
const conditions: ConditionLevel[] = ['NEW', 'ALMOST_NEW', 'GOOD', 'WORN'];

async function load() {
  loading.value = true;
  const res = await api.listProducts({
    campus: auth.browseCampus === 'ALL' ? undefined : auth.browseCampus,
    categoryId: categoryId.value || undefined,
    keyword: keyword.value || undefined,
    condition: condition.value || undefined,
    minPrice: minPrice.value ?? undefined,
    maxPrice: maxPrice.value ?? undefined,
    sort: sort.value,
    viewerCampus: auth.viewerCampus,
  });
  if (isOk(res)) results.value = res.data;
  loading.value = false;
}

let t: ReturnType<typeof setTimeout>;
watch([keyword, categoryId, sort, condition, minPrice, maxPrice, () => auth.browseCampus], () => {
  clearTimeout(t);
  t = setTimeout(load, 200);
});
onMounted(load);
</script>

<template>
  <div>
    <header class="srch-top">
      <div class="srch-bar">
        <span>🔍</span>
        <input class="srch-input" v-model="keyword" placeholder="搜索商品" />
        <button v-if="keyword" class="srch-clear" @click="keyword = ''">✕</button>
      </div>
      <button class="srch-filter" :class="{ 'is-on': showFilter }" @click="showFilter = !showFilter">筛选</button>
    </header>

    <!-- 分类 chips -->
    <div class="srch-cats">
      <button class="srch-chip" :class="{ 'is-active': categoryId === '' }" @click="categoryId = ''">全部</button>
      <button
        v-for="c in catalog.categories"
        :key="c.id"
        class="srch-chip"
        :class="{ 'is-active': categoryId === c.id }"
        @click="categoryId = c.id"
      >
        {{ c.icon }} {{ c.name }}
      </button>
    </div>

    <!-- 排序 -->
    <div class="srch-sorts">
      <button
        v-for="s in sorts"
        :key="s.key"
        class="srch-sort"
        :class="{ 'is-active': sort === s.key }"
        @click="sort = s.key"
      >
        {{ s.label }}
      </button>
    </div>

    <!-- 高级筛选 -->
    <div v-if="showFilter" class="srch-adv">
      <div class="srch-adv__row">
        <span class="srch-adv__label">价格</span>
        <input class="srch-adv__price" type="number" v-model.number="minPrice" placeholder="最低" />
        <span class="faint">—</span>
        <input class="srch-adv__price" type="number" v-model.number="maxPrice" placeholder="最高" />
      </div>
      <div class="srch-adv__row">
        <span class="srch-adv__label">成色</span>
        <div class="srch-adv__conds">
          <button class="srch-chip" :class="{ 'is-active': condition === '' }" @click="condition = ''">不限</button>
          <button
            v-for="c in conditions"
            :key="c"
            class="srch-chip"
            :class="{ 'is-active': condition === c }"
            @click="condition = c"
          >
            {{ CONDITION_LABEL[c] }}
          </button>
        </div>
      </div>
    </div>

    <!-- 结果 -->
    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="results.length === 0" class="empty">
      <div class="empty__emoji">🔍</div>
      <div class="empty__text">没有找到相关闲置，换个关键词试试</div>
    </div>
    <div v-else class="pgrid">
      <ProductCard v-for="p in results" :key="p.id" :card="p" />
    </div>
  </div>
</template>

<style scoped>
.srch-top {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}
.srch-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--c-surface-2);
  border-radius: 999px;
  padding: 8px 12px;
}
.srch-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 14px;
}
.srch-clear {
  border: none;
  background: var(--c-text-faint);
  color: #fff;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  font-size: 10px;
  line-height: 16px;
  padding: 0;
}
.srch-filter {
  border: none;
  background: none;
  font-size: 14px;
  color: var(--c-text-soft);
}
.srch-filter.is-on {
  color: var(--c-primary);
  font-weight: 600;
}
.srch-cats {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}
.srch-chip {
  flex-shrink: 0;
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 5px 11px;
  border-radius: 999px;
  white-space: nowrap;
}
.srch-chip.is-active {
  background: var(--c-primary-soft);
  border-color: var(--c-primary);
  color: var(--c-primary-dark);
  font-weight: 600;
}
.srch-sorts {
  display: flex;
  gap: 16px;
  padding: 10px 14px;
  background: var(--c-surface);
  font-size: 13px;
}
.srch-sort {
  border: none;
  background: none;
  color: var(--c-text-soft);
}
.srch-sort.is-active {
  color: var(--c-primary);
  font-weight: 700;
}
.srch-adv {
  background: var(--c-surface);
  padding: 4px 14px 14px;
  border-bottom: 1px solid var(--c-border);
}
.srch-adv__row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.srch-adv__label {
  font-size: 13px;
  color: var(--c-text-soft);
  width: 36px;
}
.srch-adv__price {
  width: 80px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 6px 8px;
  font-size: 13px;
}
.srch-adv__conds {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
