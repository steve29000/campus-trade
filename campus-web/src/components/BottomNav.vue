<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useChatStore } from '../stores/chat';

defineProps<{ active: string }>();
const router = useRouter();
const chat = useChatStore();
const unread = computed(() => chat.totalUnread);

const tabs = [
  { key: 'home', label: '首页', icon: '🏠', to: '/' },
  { key: 'search', label: '分类', icon: '🔍', to: '/search' },
  { key: 'publish', label: '发布', icon: '➕', to: '/publish' },
  { key: 'messages', label: '消息', icon: '💬', to: '/messages' },
  { key: 'me', label: '我的', icon: '👤', to: '/me' },
] as const;
</script>

<template>
  <nav class="bottomnav">
    <button
      v-for="t in tabs"
      :key="t.key"
      class="bottomnav__item"
      :class="{ 'is-active': active === t.key, 'is-publish': t.key === 'publish' }"
      @click="router.push(t.to)"
    >
      <span class="bottomnav__icon">{{ t.icon }}</span>
      <span class="bottomnav__label">{{ t.label }}</span>
      <span v-if="t.key === 'messages' && unread > 0" class="bottomnav__badge">{{ unread }}</span>
    </button>
  </nav>
</template>

<style scoped>
.bottomnav {
  flex-shrink: 0;
  height: var(--nav-h);
  display: flex;
  background: var(--c-surface);
  border-top: 1px solid var(--c-border);
  z-index: 30;
}
.bottomnav__item {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  background: none;
  border: none;
  color: var(--c-text-faint);
  font-size: 11px;
}
.bottomnav__item.is-active {
  color: var(--c-primary);
}
.bottomnav__icon {
  font-size: 20px;
  line-height: 1;
}
.bottomnav__item.is-publish .bottomnav__icon {
  width: 34px;
  height: 34px;
  margin-top: -14px;
  border-radius: 50%;
  background: var(--c-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}
.bottomnav__badge {
  position: absolute;
  top: 4px;
  right: 50%;
  transform: translateX(16px);
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--c-price);
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
}
</style>
