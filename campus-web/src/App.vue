<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import BottomNav from './components/BottomNav.vue';
import Toast from './components/Toast.vue';

const route = useRoute();
const showTab = computed(() => Boolean(route.meta.tab));
</script>

<template>
  <div class="app-frame">
    <div class="screen" :class="{ 'screen--tab': showTab }">
      <div class="screen__body">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </div>
      <BottomNav v-if="showTab" :active="(route.meta.tab as string)" />
    </div>
    <Toast />
  </div>
</template>
