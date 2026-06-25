<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import ProductCard from '../components/ProductCard.vue';
import { useAuthStore } from '../stores/auth';
import { useCatalogStore } from '../stores/catalog';
import { api } from '../api/client';
import { isOk, type ProductCard as Card } from '../api/types';
import { pseudoDistanceKm } from '../domain/campus';
import type { BrowseCampus } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();
const catalog = useCatalogStore();

const products = ref<Card[]>([]);
const loading = ref(true);

const campusOptions: { key: BrowseCampus; label: string }[] = [
  { key: 'ALL', label: '全部校区' },
  { key: 'SOUTH', label: '南校区' },
  { key: 'NORTH', label: '北校区' },
];

const nearby = computed(() => {
  const viewer = auth.viewerCampus;
  return [...products.value]
    .sort((a, b) => pseudoDistanceKm(viewer, a) - pseudoDistanceKm(viewer, b))
    .slice(0, 4);
});
const latest = computed(() => products.value.slice(0, 8));
const aiPick = computed(() => products.value.find((p) => p.favoriteCount >= 30) ?? products.value[0]);

async function load() {
  loading.value = true;
  const campus = auth.browseCampus === 'ALL' ? undefined : auth.browseCampus;
  const res = await api.listProducts({ campus, sort: 'NEWEST', viewerCampus: auth.viewerCampus });
  if (isOk(res)) products.value = res.data;
  loading.value = false;
}

watch(() => auth.browseCampus, load);
onMounted(load);

function goCategory(id: string) {
  router.push({ name: 'search', query: { cat: id } });
}
</script>

<template>
  <div>
    <!-- 顶部：校区 + 搜索 -->
    <header class="home-top">
      <div class="home-campus">
        <button
          v-for="o in campusOptions"
          :key="o.key"
          class="home-campus__chip"
          :class="{ 'is-active': auth.browseCampus === o.key }"
          @click="auth.browseCampus = o.key"
        >
          {{ o.label }}
        </button>
      </div>
      <div class="home-search" @click="router.push('/search')">
        <span>🔍</span><span class="home-search__ph">搜索台灯 / 教材 / 自行车…</span>
      </div>
    </header>

    <!-- 分类入口 -->
    <div class="home-cats">
      <button v-for="c in catalog.categories" :key="c.id" class="home-cat" @click="goCategory(c.id)">
        <span class="home-cat__icon">{{ c.icon }}</span>
        <span class="home-cat__name">{{ c.name }}</span>
      </button>
    </div>

    <!-- AI 辅助入口（克制，不做资本主义庙会） -->
    <div class="home-ai" @click="router.push('/publish')">
      <div class="home-ai__emoji">🤖</div>
      <div class="home-ai__text">
        <strong>AI 估价 + AI 文案</strong>
        <span class="faint">填几个字，30 秒发布闲置</span>
      </div>
      <span class="home-ai__go">去发布 ›</span>
    </div>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>

    <template v-else>
      <!-- AI 推荐单品 -->
      <section v-if="aiPick" class="section">
        <div class="section__head" style="padding-left:0;padding-right:0">
          <span class="section__title">🌟 为你推荐</span>
        </div>
        <article class="ai-pick" @click="router.push(`/product/${aiPick.id}`)">
          <div class="ai-pick__thumb">{{ catalog.categoryIcon(aiPick.categoryId) }}</div>
          <div class="ai-pick__info">
            <div class="ai-pick__title">{{ aiPick.title }}</div>
            <div class="ai-pick__meta faint">{{ aiPick.favoriteCount }} 人想要 · {{ aiPick.viewCount }} 浏览</div>
            <div class="price" style="font-size:18px"><span class="price__symbol">¥</span>{{ aiPick.price }}</div>
          </div>
        </article>
      </section>

      <!-- 附近好物 -->
      <section>
        <div class="section__head">
          <span class="section__title">📍 附近好物</span>
          <span class="section__more" @click="router.push('/search')">更多 ›</span>
        </div>
        <div class="pgrid" style="padding-top:0">
          <ProductCard v-for="p in nearby" :key="p.id" :card="p" />
        </div>
      </section>

      <!-- 最新发布 -->
      <section>
        <div class="section__head">
          <span class="section__title">🆕 最新发布</span>
        </div>
        <div class="pgrid" style="padding-top:0">
          <ProductCard v-for="p in latest" :key="p.id" :card="p" />
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.home-top {
  background: var(--c-surface);
  padding: 12px 12px 14px;
  position: sticky;
  top: 0;
  z-index: 20;
  border-bottom: 1px solid var(--c-border);
}
.home-campus {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.home-campus__chip {
  border: none;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 5px 12px;
  border-radius: 999px;
}
.home-campus__chip.is-active {
  background: var(--c-primary);
  color: #fff;
  font-weight: 600;
}
.home-search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--c-surface-2);
  border-radius: 999px;
  padding: 9px 14px;
  color: var(--c-text-faint);
  font-size: 14px;
}
.home-cats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px 0;
  padding: 16px 8px;
  background: var(--c-surface);
}
.home-cat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  font-size: 12px;
  color: var(--c-text-soft);
}
.home-cat__icon {
  font-size: 26px;
}
.home-ai {
  margin: 12px;
  padding: 12px 14px;
  background: linear-gradient(110deg, #ecfdf5, #eff6ff);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  gap: 12px;
}
.home-ai__emoji {
  font-size: 28px;
}
.home-ai__text {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  gap: 2px;
}
.home-ai__text strong {
  font-size: 14px;
}
.home-ai__go {
  margin-left: auto;
  color: var(--c-primary-dark);
  font-size: 13px;
  font-weight: 600;
}
.ai-pick {
  margin: 0 12px;
  background: var(--c-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  display: flex;
  gap: 12px;
  padding: 12px;
  align-items: center;
}
.ai-pick__thumb {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-sm);
  background: var(--c-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}
.ai-pick__info {
  flex: 1;
  min-width: 0;
}
.ai-pick__title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ai-pick__meta {
  font-size: 11px;
  margin: 3px 0 4px;
}

/* ---------- 桌面：模块居中铺开 ---------- */
@media (min-width: 900px) {
  .home-cats {
    max-width: 1180px;
    margin: 20px auto 0;
    grid-template-columns: repeat(8, 1fr);
    border-radius: var(--radius);
    padding: 18px 12px;
    box-shadow: var(--shadow-card);
  }
  .home-cat {
    font-size: 13px;
  }
  .home-cat__icon {
    font-size: 30px;
  }
  .home-ai {
    max-width: 1180px;
    margin: 18px auto;
    padding: 18px 22px;
  }
  .home-ai__emoji {
    font-size: 34px;
  }
  .home-ai__text {
    font-size: 13px;
  }
  .home-ai__text strong {
    font-size: 16px;
  }
  .ai-pick {
    max-width: 1180px;
    margin: 0 auto;
  }
}
</style>
