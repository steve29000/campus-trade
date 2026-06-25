<script setup lang="ts">
import { useRouter } from 'vue-router';
defineProps<{ title: string; phase: string; emoji?: string; desc?: string; back?: boolean }>();
const router = useRouter();
</script>

<template>
  <div>
    <header class="topbar">
      <button v-if="back" class="topbar__back" @click="router.back()">‹</button>
      <span class="topbar__title">{{ title }}</span>
    </header>
    <div class="empty" style="padding-top:90px">
      <div class="empty__emoji">{{ emoji || '🚧' }}</div>
      <div class="empty__text">{{ desc || '该模块即将上线' }}</div>
      <div class="faint" style="margin-top:8px;font-size:12px">规划于 {{ phase }}</div>
    </div>
  </div>
</template>
