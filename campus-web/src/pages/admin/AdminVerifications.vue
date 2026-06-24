<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from '../../composables/toast';
import { api } from '../../api/client';
import { isOk, type VerificationView } from '../../api/types';
import { VERIFY_STATUS_LABEL } from '../../domain/types';
import { campusName } from '../../domain/campus';
import { timeAgo } from '../../domain/format';

const router = useRouter();
const toast = useToast();
const list = ref<VerificationView[]>([]);
const loading = ref(true);

async function load() {
  loading.value = true;
  const res = await api.adminListVerifications();
  if (isOk(res)) list.value = res.data;
  loading.value = false;
}

async function review(v: VerificationView, approve: boolean) {
  let reason: string | undefined;
  if (!approve) {
    reason = window.prompt('驳回原因') || '信息不完整';
  }
  const res = await api.reviewVerification(v.id, approve, reason);
  if (isOk(res)) {
    toast.show(approve ? '已通过' : '已驳回');
    await load();
  }
}

onMounted(load);
</script>

<template>
  <div>
    <header class="topbar">
      <button class="topbar__back" @click="router.push('/admin')">‹</button>
      <span class="topbar__title">认证审核</span>
    </header>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="list.length === 0" class="empty"><div class="empty__emoji">🎓</div><div class="empty__text">暂无认证申请</div></div>
    <div v-else class="vlist">
      <div v-for="v in list" :key="v.id" class="vcard">
        <div class="vcard__head">
          <span class="vcard__avatar">{{ v.user.avatar }}</span>
          <div class="vcard__who">
            <div class="vcard__name">{{ v.user.nickname }}</div>
            <div class="faint" style="font-size:12px">{{ timeAgo(v.createdAt) }}提交</div>
          </div>
          <span class="vcard__status" :class="`st-${v.status}`">{{ VERIFY_STATUS_LABEL[v.status] }}</span>
        </div>
        <div class="vcard__info">
          <div>学校：{{ v.school }}</div>
          <div>校区：{{ campusName(v.campus) }}</div>
          <div>学号：{{ v.studentNo }}</div>
          <div v-if="v.rejectReason" class="faint">驳回原因：{{ v.rejectReason }}</div>
        </div>
        <div v-if="v.status === 'PENDING'" class="vcard__acts">
          <button class="btn btn--primary" @click="review(v, true)">通过</button>
          <button class="btn btn--ghost" @click="review(v, false)">驳回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vlist {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.vcard {
  background: var(--c-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px;
}
.vcard__head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.vcard__avatar {
  font-size: 28px;
}
.vcard__who {
  flex: 1;
}
.vcard__name {
  font-size: 15px;
  font-weight: 600;
}
.vcard__status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--c-surface-2);
  color: var(--c-text-soft);
}
.vcard__status.st-PENDING {
  background: #fffbeb;
  color: #b45309;
}
.vcard__status.st-VERIFIED {
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
}
.vcard__status.st-REJECTED {
  background: #fee2e2;
  color: #b91c1c;
}
.vcard__info {
  margin: 10px 0;
  font-size: 13px;
  color: var(--c-text);
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.vcard__acts {
  display: flex;
  gap: 10px;
}
.vcard__acts .btn {
  flex: 1;
}
</style>
