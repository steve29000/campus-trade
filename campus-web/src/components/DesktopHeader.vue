<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore, type BrowseCampus } from '../stores/auth';
import { useChatStore } from '../stores/chat';
import { useFavoriteStore } from '../stores/favorites';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const chat = useChatStore();
const favorites = useFavoriteStore();

const kw = ref((route.query.kw as string) || '');
const menuOpen = ref(false);
const unread = computed(() => chat.totalUnread);

const campusOptions: { key: BrowseCampus; label: string }[] = [
  { key: 'ALL', label: '全部' },
  { key: 'SOUTH', label: '南校区' },
  { key: 'NORTH', label: '北校区' },
];

const links = [
  { key: 'home', label: '首页', to: '/' },
  { key: 'search', label: '分类', to: '/search' },
  { key: 'publish', label: '发布', to: '/publish' },
  { key: 'messages', label: '消息', to: '/messages' },
  { key: 'me', label: '我的', to: '/me' },
] as const;

function search() {
  router.push({ name: 'search', query: kw.value.trim() ? { kw: kw.value.trim() } : {} });
}

function logout() {
  menuOpen.value = false;
  auth.logout();
  favorites.clear();
  chat.clear();
  router.replace('/');
}
</script>

<template>
  <header class="dh">
    <div class="dh__inner container">
      <!-- logo -->
      <button class="dh__logo" @click="router.push('/')">
        <span class="dh__logo-mark">🛍️</span>
        <span class="dh__logo-text">校园集市</span>
      </button>

      <!-- 搜索 -->
      <div class="dh__search">
        <span class="dh__search-icon">🔍</span>
        <input
          class="dh__search-input"
          v-model="kw"
          placeholder="搜索台灯 / 教材 / 自行车…"
          @keyup.enter="search"
        />
        <button class="dh__search-btn" @click="search">搜索</button>
      </div>

      <!-- 校区 -->
      <div class="dh__campus">
        <button
          v-for="o in campusOptions"
          :key="o.key"
          class="dh__campus-chip"
          :class="{ 'is-active': auth.browseCampus === o.key }"
          @click="auth.browseCampus = o.key"
        >
          {{ o.label }}
        </button>
      </div>

      <!-- 导航 -->
      <nav class="dh__nav">
        <button
          v-for="l in links"
          :key="l.key"
          class="dh__link"
          :class="{ 'is-active': route.meta.tab === l.key }"
          @click="router.push(l.to)"
        >
          {{ l.label }}
          <span v-if="l.key === 'messages' && unread > 0" class="dh__badge">{{ unread }}</span>
        </button>
      </nav>

      <!-- 用户 -->
      <div class="dh__user">
        <button v-if="!auth.isAuthed" class="btn btn--primary dh__login" @click="router.push({ name: 'login', query: { redirect: route.fullPath } })">
          登录
        </button>
        <div v-else class="dh__account">
          <button class="dh__avatar" @click="menuOpen = !menuOpen">
            <span>{{ auth.user!.avatar }}</span>
            <span class="dh__avatar-name">{{ auth.user!.nickname }}</span>
            <span class="dh__caret">▾</span>
          </button>
          <template v-if="menuOpen">
            <div class="dh__overlay" @click="menuOpen = false"></div>
            <div class="dh__menu">
              <button class="dh__menu-item" @click="router.push('/me'); menuOpen = false">个人中心</button>
              <button class="dh__menu-item" @click="router.push('/my-listings'); menuOpen = false">我的发布</button>
              <button class="dh__menu-item" @click="router.push('/favorites'); menuOpen = false">我的收藏</button>
              <button v-if="auth.isAdmin" class="dh__menu-item" @click="router.push('/admin'); menuOpen = false">管理后台</button>
              <button class="dh__menu-item dh__menu-item--danger" @click="logout">退出登录</button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.dh {
  display: none; /* 手机端隐藏，>=900px 显示 */
  position: sticky;
  top: 0;
  z-index: 40;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}
@media (min-width: 900px) {
  .dh {
    display: block;
  }
}
.dh__inner {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 62px;
}
.dh__logo {
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: none;
  flex-shrink: 0;
}
.dh__logo-mark {
  font-size: 24px;
}
.dh__logo-text {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
  color: var(--c-text);
}
.dh__search {
  flex: 1;
  max-width: 460px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--c-surface-2);
  border: 1px solid var(--c-border);
  border-radius: 999px;
  padding: 6px 6px 6px 14px;
}
.dh__search-icon {
  font-size: 14px;
}
.dh__search-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 14px;
}
.dh__search-btn {
  border: none;
  background: var(--c-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding: 7px 16px;
  border-radius: 999px;
}
.dh__campus {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.dh__campus-chip {
  border: none;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 999px;
}
.dh__campus-chip.is-active {
  background: var(--c-primary);
  color: #fff;
  font-weight: 600;
}
.dh__nav {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.dh__link {
  position: relative;
  border: none;
  background: none;
  color: var(--c-text-soft);
  font-size: 14px;
  font-weight: 500;
  padding: 8px 10px;
  border-radius: 8px;
}
.dh__link:hover {
  background: var(--c-surface-2);
  color: var(--c-text);
}
.dh__link.is-active {
  color: var(--c-primary);
  font-weight: 700;
}
.dh__badge {
  position: absolute;
  top: 0;
  right: 0;
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
.dh__user {
  flex-shrink: 0;
}
.dh__login {
  padding: 8px 20px;
}
.dh__account {
  position: relative;
}
.dh__avatar {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  border-radius: 999px;
  padding: 5px 10px 5px 8px;
  font-size: 18px;
}
.dh__avatar-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dh__caret {
  font-size: 11px;
  color: var(--c-text-faint);
}
.dh__overlay {
  position: fixed;
  inset: 0;
  z-index: 41;
}
.dh__menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  z-index: 42;
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-pop);
  padding: 6px;
  min-width: 150px;
}
.dh__menu-item {
  display: block;
  width: 100%;
  text-align: left;
  border: none;
  background: none;
  padding: 9px 12px;
  font-size: 14px;
  border-radius: var(--radius-sm);
  color: var(--c-text);
}
.dh__menu-item:hover {
  background: var(--c-surface-2);
}
.dh__menu-item--danger {
  color: var(--c-danger);
}
</style>
