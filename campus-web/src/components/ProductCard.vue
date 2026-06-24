<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import ProductThumb from './ProductThumb.vue';
import { useCatalogStore } from '../stores/catalog';
import { useAuthStore } from '../stores/auth';
import { useFavoriteStore } from '../stores/favorites';
import { pseudoDistanceKm, formatDistance, campusName } from '../domain/campus';
import { CONDITION_LABEL, PRODUCT_STATUS_LABEL } from '../domain/types';
import type { ProductCard } from '../api/types';

const props = defineProps<{ card: ProductCard }>();
const statusOverlay = computed(() =>
  props.card.status === 'ON_SALE' ? '' : PRODUCT_STATUS_LABEL[props.card.status],
);
const router = useRouter();
const catalog = useCatalogStore();
const auth = useAuthStore();
const favorites = useFavoriteStore();

const emoji = computed(() => catalog.categoryIcon(props.card.categoryId));
const distance = computed(() => formatDistance(pseudoDistanceKm(auth.viewerCampus, props.card)));
const faved = computed(() => favorites.has(props.card.id));

async function toggleFav(e: Event) {
  e.stopPropagation();
  const r = await favorites.toggle(props.card.id);
  if (r === null) router.push({ name: 'login', query: { redirect: '/' } });
}
</script>

<template>
  <article class="pcard" @click="router.push(`/product/${card.id}`)">
    <div class="pcard__media">
      <ProductThumb :seed="card.id" :emoji="emoji" />
      <span v-if="statusOverlay" class="pcard__status">{{ statusOverlay }}</span>
      <button class="pcard__fav" :class="{ 'is-on': faved }" @click="toggleFav">
        {{ faved ? '❤️' : '🤍' }}
      </button>
    </div>
    <div class="pcard__body">
      <h3 class="pcard__title">{{ card.title }}</h3>
      <div class="pcard__price">
        <span class="price"><span class="price__symbol">¥</span>{{ card.price }}</span>
        <span v-if="card.originalPrice" class="price__origin">¥{{ card.originalPrice }}</span>
      </div>
      <div class="pcard__tags">
        <span class="tag tag--condition">{{ CONDITION_LABEL[card.conditionLevel] }}</span>
        <span class="tag tag--distance">📍 {{ distance }}</span>
      </div>
      <div class="pcard__seller">
        <span class="pcard__avatar">{{ card.seller.avatar }}</span>
        <span class="pcard__nick">{{ card.seller.nickname }}</span>
        <span v-if="card.seller.verifyStatus === 'VERIFIED'" class="tag tag--verified">✓ 认证</span>
        <span class="pcard__campus faint">{{ campusName(card.campus) }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.pcard {
  background: var(--c-surface);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}
.pcard__media {
  position: relative;
}
.pcard__status {
  position: absolute;
  left: 8px;
  top: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
}
.pcard__fav {
  position: absolute;
  right: 6px;
  top: 6px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pcard__body {
  padding: 8px 9px 10px;
}
.pcard__title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 500;
  line-height: 1.35;
  color: var(--c-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pcard__price {
  margin-top: 6px;
  font-size: 17px;
}
.pcard__tags {
  margin-top: 6px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.pcard__seller {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--c-text-soft);
}
.pcard__avatar {
  font-size: 14px;
}
.pcard__nick {
  max-width: 70px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pcard__campus {
  margin-left: auto;
}
</style>
