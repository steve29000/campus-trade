<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import BottomNav from './components/BottomNav.vue';
import DesktopHeader from './components/DesktopHeader.vue';
import Toast from './components/Toast.vue';

const route = useRoute();
const showTab = computed(() => Boolean(route.meta.tab));
// 后台路由自带控制台外壳（AdminLayout），不显示学生端顶栏
const isAdminRoute = computed(() => route.path.startsWith('/admin'));
</script>

<template>
  <div class="app-frame">
    <!-- 桌面顶栏（CSS 控制：<900px 隐藏；后台路由不显示） -->
    <DesktopHeader v-if="!isAdminRoute" />
    <div class="screen">
      <div class="screen__body">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </div>
      <!-- 手机底栏（CSS 控制：>=900px 隐藏） -->
      <BottomNav v-if="showTab" :active="(route.meta.tab as string)" />
    </div>
    <Toast />
  </div>
</template>
