<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useFavoriteStore } from '../stores/favorites';
import { useChatStore } from '../stores/chat';

// 后台控制台外壳：桌面左侧栏 + 内容区；手机顶栏 + 内容。
const props = withDefaults(defineProps<{ title: string; back?: string }>(), { back: '/admin' });
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const favorites = useFavoriteStore();
const chat = useChatStore();

const nav = [
  { label: '数据概览', icon: '📊', name: 'admin', to: '/admin' },
  { label: '用户管理', icon: '👥', name: 'admin-users', to: '/admin/users' },
  { label: '商品管理', icon: '📦', name: 'admin-products', to: '/admin/products' },
  { label: '认证审核', icon: '🎓', name: 'admin-verifications', to: '/admin/verifications' },
  { label: '举报管理', icon: '🚩', name: 'admin-reports', to: '/admin/reports' },
];

function logout() {
  auth.logout();
  favorites.clear();
  chat.clear();
  router.replace('/admin/login');
}

// 引用 props.back 以避免未使用告警（模板里通过 props 使用）
void props;
</script>

<template>
  <div class="alayout">
    <!-- 桌面侧边栏 -->
    <aside class="alayout__side">
      <div class="alayout__brand"><span class="alayout__brand-mark">🛡️</span>管理后台</div>
      <nav class="alayout__nav">
        <button
          v-for="n in nav"
          :key="n.name"
          class="alayout__navlink"
          :class="{ 'is-active': route.name === n.name }"
          @click="router.push(n.to)"
        >
          <span class="alayout__navicon">{{ n.icon }}</span>{{ n.label }}
        </button>
      </nav>
      <div class="alayout__side-foot">
        <button class="alayout__foot-btn" @click="router.push('/')">← 返回学生端</button>
        <button class="alayout__foot-btn" @click="logout">退出登录</button>
      </div>
    </aside>

    <!-- 内容区 -->
    <div class="alayout__content">
      <!-- 手机顶栏 -->
      <header class="topbar alayout__mobilebar">
        <button class="topbar__back" @click="router.push(back)">‹</button>
        <span class="topbar__title">{{ title }}</span>
        <button class="alayout__mlogout" @click="logout">退出</button>
      </header>
      <!-- 桌面内容标题 -->
      <div class="alayout__head">{{ title }}</div>
      <div class="alayout__body">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.alayout__side {
  display: none;
}
.alayout__head {
  display: none;
}
.alayout__mlogout {
  margin-left: auto;
  border: 1px solid var(--c-border);
  background: none;
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 5px 12px;
  border-radius: 999px;
}

/* ---------- 桌面：左侧栏 + 内容 ---------- */
@media (min-width: 900px) {
  .alayout {
    display: flex;
  }
  .alayout__side {
    display: flex;
    flex-direction: column;
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    width: 220px;
    background: #1f2937;
    color: #e5e7eb;
    padding: 20px 14px;
    z-index: 30;
  }
  .alayout__brand {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 17px;
    font-weight: 700;
    color: #fff;
    padding: 0 8px 18px;
  }
  .alayout__brand-mark {
    font-size: 22px;
  }
  .alayout__nav {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .alayout__navlink {
    display: flex;
    align-items: center;
    gap: 10px;
    border: none;
    background: none;
    color: #cbd5e1;
    font-size: 14px;
    text-align: left;
    padding: 11px 12px;
    border-radius: var(--radius-sm);
  }
  .alayout__navlink:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #fff;
  }
  .alayout__navlink.is-active {
    background: var(--c-primary);
    color: #fff;
    font-weight: 600;
  }
  .alayout__navicon {
    font-size: 16px;
  }
  .alayout__side-foot {
    margin-top: auto;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .alayout__foot-btn {
    border: none;
    background: none;
    color: #94a3b8;
    font-size: 13px;
    text-align: left;
    padding: 9px 12px;
    border-radius: var(--radius-sm);
  }
  .alayout__foot-btn:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #fff;
  }
  .alayout__content {
    margin-left: 220px;
    flex: 1;
    min-width: 0;
  }
  .alayout__mobilebar {
    display: none;
  }
  .alayout__head {
    display: block;
    font-size: 22px;
    font-weight: 700;
    padding: 26px 28px 6px;
    max-width: 1040px;
    margin: 0 auto;
  }
  .alayout__body {
    max-width: 1040px;
    margin: 0 auto;
    padding: 8px 28px 60px;
  }
}
</style>
