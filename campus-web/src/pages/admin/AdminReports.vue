<script setup lang="ts">
import { ref, onMounted } from 'vue';
import AdminLayout from '../../components/AdminLayout.vue';
import { useToast } from '../../composables/toast';
import { api } from '../../api/client';
import { isOk, type ReportView } from '../../api/types';
import { timeAgo } from '../../domain/format';

const toast = useToast();
const list = ref<ReportView[]>([]);
const loading = ref(true);

const STATUS_LABEL: Record<string, string> = { PENDING: '待处理', RESOLVED: '已处理', DISMISSED: '已驳回' };

async function load() {
  loading.value = true;
  const res = await api.adminListReports();
  if (isOk(res)) list.value = res.data;
  loading.value = false;
}

async function resolve(r: ReportView, delist: boolean) {
  const res = await api.adminHandleReport(r.id, 'RESOLVED', delist);
  if (isOk(res)) {
    toast.show(delist ? '已处理并下架' : '已处理');
    await load();
  }
}

async function dismiss(r: ReportView) {
  const res = await api.adminHandleReport(r.id, 'DISMISSED');
  if (isOk(res)) {
    toast.show('已驳回举报');
    await load();
  }
}
onMounted(load);
</script>

<template>
  <AdminLayout title="举报管理">
    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="list.length === 0" class="empty"><div class="empty__emoji">🚩</div><div class="empty__text">暂无举报</div></div>
    <div v-else class="rlist">
      <div v-for="r in list" :key="r.id" class="rcard">
        <div class="rcard__head">
          <span class="rcard__reason">{{ r.reason }}</span>
          <span class="rcard__status" :class="`st-${r.status}`">{{ STATUS_LABEL[r.status] }}</span>
        </div>
        <div class="rcard__target">
          {{ r.targetType === 'PRODUCT' ? '商品' : '用户' }}：{{ r.targetTitle }}
        </div>
        <div v-if="r.description" class="rcard__desc faint">“{{ r.description }}”</div>
        <div class="rcard__foot faint">举报人 {{ r.reporter.nickname }} · {{ timeAgo(r.createdAt) }}</div>
        <div v-if="r.status === 'PENDING'" class="rcard__acts">
          <button v-if="r.targetType === 'PRODUCT'" class="btn btn--primary" @click="resolve(r, true)">下架并处理</button>
          <button class="btn btn--ghost" @click="resolve(r, false)">仅标记处理</button>
          <button class="btn btn--ghost" @click="dismiss(r)">驳回</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<style scoped>
.rlist {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rcard {
  background: var(--c-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px;
}
.rcard__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rcard__reason {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-danger);
}
.rcard__status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
}
.rcard__status.st-PENDING {
  background: #fffbeb;
  color: #b45309;
}
.rcard__target {
  margin: 8px 0 4px;
  font-size: 14px;
}
.rcard__desc {
  font-size: 13px;
  line-height: 1.5;
}
.rcard__foot {
  font-size: 12px;
  margin-top: 8px;
}
.rcard__acts {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}
.rcard__acts .btn {
  flex: 1;
  font-size: 13px;
  padding: 8px;
  white-space: nowrap;
}

@media (min-width: 900px) {
  .rlist {
    display: grid;
    grid-template-columns: 1fr 1fr;
    padding: 12px 0;
  }
}
</style>
