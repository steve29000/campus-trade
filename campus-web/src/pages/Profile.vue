<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useFavoriteStore } from '../stores/favorites';
import { useChatStore } from '../stores/chat';
import { useToast } from '../composables/toast';
import { campusName } from '../domain/campus';
import { VERIFY_STATUS_LABEL } from '../domain/types';

const router = useRouter();
const auth = useAuthStore();
const favorites = useFavoriteStore();
const chat = useChatStore();
const toast = useToast();

const entries = [
  { icon: '📦', label: '我的发布', to: '/my-listings' },
  { icon: '❤️', label: '我的收藏', to: '/favorites' },
  { icon: '🛒', label: '我买到的', soon: true },
  { icon: '💰', label: '我卖出的', soon: true },
  { icon: '🕘', label: '浏览记录', soon: true },
  { icon: '⚙️', label: '账号设置', soon: true },
] as const;

function go(e: (typeof entries)[number]) {
  if ('soon' in e && e.soon) return toast.show('该功能后续版本上线');
  if ('to' in e) router.push(e.to);
}

function logout() {
  auth.logout();
  favorites.clear();
  chat.clear();
  router.replace('/');
}
</script>

<template>
  <div class="me">
    <!-- 未登录 -->
    <div v-if="!auth.user" class="me-guest">
      <div class="me-guest__avatar">👋</div>
      <p class="me-guest__text">登录后即可发布闲置、收藏、和同学聊天</p>
      <button class="btn btn--primary btn--lg" @click="router.push({ name: 'login', query: { redirect: '/me' } })">
        登录 / 注册
      </button>
    </div>

    <!-- 已登录 -->
    <template v-else>
      <header class="me-head">
        <span class="me-head__avatar">{{ auth.user.avatar }}</span>
        <div class="me-head__info">
          <div class="me-head__name">{{ auth.user.nickname }}</div>
          <div class="me-head__sub">
            <span class="tag" :class="auth.isVerified ? 'tag--verified' : ''">
              {{ auth.isVerified ? '✓ ' : '' }}{{ VERIFY_STATUS_LABEL[auth.user.verifyStatus] }}
            </span>
            <span class="faint">{{ campusName(auth.user.campus) }} · 信用 {{ auth.user.rating }}</span>
          </div>
        </div>
      </header>

      <!-- 认证引导 -->
      <div v-if="!auth.isVerified" class="me-verify" @click="router.push('/verify')">
        <span>🎓</span>
        <div class="me-verify__text">
          <strong>{{ auth.user.verifyStatus === 'PENDING' ? '认证审核中' : '完成校园认证' }}</strong>
          <span class="faint">认证后才能发布商品、联系卖家</span>
        </div>
        <span class="me-verify__go">{{ auth.user.verifyStatus === 'PENDING' ? '查看 ›' : '去认证 ›' }}</span>
      </div>

      <!-- 功能入口 -->
      <div class="me-grid card">
        <button v-for="e in entries" :key="e.label" class="me-cell" @click="go(e)">
          <span class="me-cell__icon">{{ e.icon }}</span>
          <span class="me-cell__label">{{ e.label }}</span>
        </button>
      </div>

      <button v-if="auth.isAdmin" class="me-admin" @click="router.push('/admin')">
        🛡️ 进入管理后台 <span class="me-admin__go">›</span>
      </button>

      <button class="btn btn--ghost btn--block" style="margin:18px 14px;width:auto" @click="logout">退出登录</button>
    </template>
  </div>
</template>

<style scoped>
.me-guest {
  text-align: center;
  padding: 80px 30px;
}
.me-guest__avatar {
  font-size: 56px;
}
.me-guest__text {
  color: var(--c-text-soft);
  font-size: 14px;
  margin: 14px 0 22px;
}
.me-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 18px;
  background: linear-gradient(135deg, var(--c-primary-soft), #fff);
}
.me-head__avatar {
  font-size: 46px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-card);
}
.me-head__name {
  font-size: 19px;
  font-weight: 700;
}
.me-head__sub {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.me-verify {
  margin: 12px;
  padding: 12px 14px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.me-verify span:first-child {
  font-size: 24px;
}
.me-verify__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.me-verify__go {
  margin-left: auto;
  color: #b45309;
  font-weight: 600;
}
.me-grid {
  margin: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 8px 0;
}
.me-cell {
  background: none;
  border: none;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--c-text);
}
.me-cell__icon {
  font-size: 24px;
}
.me-admin {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 12px;
  width: calc(100% - 24px);
  background: #1f2937;
  color: #fff;
  border: none;
  border-radius: var(--radius);
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
}
.me-admin__go {
  margin-left: auto;
}
</style>
