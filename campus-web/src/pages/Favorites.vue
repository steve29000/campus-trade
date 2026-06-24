<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import ProductCard from '../components/ProductCard.vue';
import { useAuthStore } from '../stores/auth';
import { api } from '../api/client';
import { isOk, type ProductCard as Card } from '../api/types';

const router = useRouter();
const auth = useAuthStore();
const list = ref<Card[]>([]);
const loading = ref(true);

onMounted(async () => {
  if (!auth.user) return;
  const res = await api.listFavorites(auth.user.id);
  if (isOk(res)) list.value = res.data;
  loading.value = false;
});
</script>

<template>
  <div>
    <header class="topbar">
      <button class="topbar__back" @click="router.back()">‹</button>
      <span class="topbar__title">我的收藏</span>
    </header>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="list.length === 0" class="empty">
      <div class="empty__emoji">🤍</div>
      <div class="empty__text">还没有收藏，去首页逛逛吧</div>
      <button class="btn btn--primary" style="margin-top:14px" @click="router.push('/')">去逛逛</button>
    </div>
    <div v-else class="pgrid">
      <ProductCard v-for="p in list" :key="p.id" :card="p" />
    </div>
  </div>
</template>
