<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AdminLayout from '../../components/AdminLayout.vue';
import { api } from '../../api/client';
import { isOk, type AdminStats } from '../../api/types';

const router = useRouter();
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

onMounted(async () => {
  const res = await api.adminStats();
  if (isOk(res)) stats.value = res.data;
});
</script>

<template>
  <AdminLayout title="数据概览" back="/">
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
  </AdminLayout>
</template>

<style scoped>
.adash-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  padding: 14px;
}
.adash-stat {
  background: var(--c-surface);
  border-radius: var(--radius);
  padding: 16px 8px;
  text-align: center;
  box-shadow: var(--shadow-card);
}
.adash-stat__icon {
  font-size: 20px;
}
.adash-stat__num {
  font-size: 24px;
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
  display: grid;
  grid-template-columns: 1fr;
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

@media (min-width: 900px) {
  .adash-entries {
    grid-template-columns: 1fr 1fr;
    padding: 14px;
  }
}
</style>
