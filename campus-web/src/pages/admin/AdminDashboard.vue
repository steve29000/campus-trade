<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { api } from '../../api/client';
import { isOk, type AdminStats } from '../../api/types';

const router = useRouter();
const auth = useAuthStore();
const stats = ref<AdminStats | null>(null);

const cards = [
  { key: 'userCount', label: '学生用户', icon: '👥' },
  { key: 'verifiedCount', label: '已认证', icon: '✅' },
  { key: 'productCount', label: '商品总数', icon: '📦' },
  { key: 'todayNewProducts', label: '今日新增', icon: '🆕' },
  { key: 'pendingVerifications', label: '待审认证', icon: '🎓' },
  { key: 'pendingReports', label: '待处理举报', icon: '🚩' },
  { key: 'completedDeals', label: '已成交', icon: '🤝' },
] as const;

const entries = [
  { label: '用户管理', icon: '👥', to: '/admin/users', desc: '查看 / 封禁 / 解封' },
  { label: '商品管理', icon: '📦', to: '/admin/products', desc: '下架 / 删除违规' },
  { label: '认证审核', icon: '🎓', to: '/admin/verifications', desc: '通过 / 驳回学籍' },
  { label: '举报管理', icon: '🚩', to: '/admin/reports', desc: '处理违规举报' },
];

function logout() {
  auth.logout();
  router.replace('/admin/login');
}

onMounted(async () => {
  const res = await api.adminStats();
  if (isOk(res)) stats.value = res.data;
});
</script>

<template>
  <div class="adash">
    <header class="adash-top">
      <div>
        <div class="adash-top__title">数据概览</div>
        <div class="adash-top__sub">校园集市管理后台</div>
      </div>
      <button class="adash-top__logout" @click="logout">退出</button>
    </header>

    <div class="adash-stats">
      <div v-for="c in cards" :key="c.key" class="adash-stat">
        <div class="adash-stat__icon">{{ c.icon }}</div>
        <div class="adash-stat__num">{{ stats ? stats[c.key] : '—' }}</div>
        <div class="adash-stat__label">{{ c.label }}</div>
      </div>
    </div>

    <div class="adash-entries">
      <button v-for="e in entries" :key="e.to" class="adash-entry" @click="router.push(e.to)">
        <span class="adash-entry__icon">{{ e.icon }}</span>
        <span class="adash-entry__main">
          <span class="adash-entry__label">{{ e.label }}</span>
          <span class="adash-entry__desc faint">{{ e.desc }}</span>
        </span>
        <span class="adash-entry__go">›</span>
      </button>
    </div>

    <button class="btn btn--ghost btn--block" style="margin:18px 14px;width:auto" @click="router.push('/')">返回学生端</button>
  </div>
</template>

<style scoped>
.adash {
  min-height: 100%;
  background: var(--c-bg);
}
.adash-top {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  background: linear-gradient(135deg, #1f2937, #334155);
  color: #fff;
}
.adash-top__title {
  font-size: 20px;
  font-weight: 700;
}
.adash-top__sub {
  font-size: 12px;
  color: #cbd5e1;
  margin-top: 2px;
}
.adash-top__logout {
  margin-left: auto;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: transparent;
  color: #fff;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 999px;
}
.adash-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px;
}
.adash-stat {
  background: var(--c-surface);
  border-radius: var(--radius);
  padding: 14px 8px;
  text-align: center;
  box-shadow: var(--shadow-card);
}
.adash-stat__icon {
  font-size: 20px;
}
.adash-stat__num {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-primary-dark);
  margin: 4px 0 2px;
}
.adash-stat__label {
  font-size: 11px;
  color: var(--c-text-soft);
}
.adash-entries {
  padding: 0 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.adash-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--c-surface);
  border: none;
  border-radius: var(--radius);
  padding: 14px;
  box-shadow: var(--shadow-card);
}
.adash-entry__icon {
  font-size: 24px;
}
.adash-entry__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.adash-entry__label {
  font-size: 15px;
  font-weight: 600;
}
.adash-entry__desc {
  font-size: 12px;
}
.adash-entry__go {
  font-size: 20px;
  color: var(--c-text-faint);
}
</style>
