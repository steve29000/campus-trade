<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import AdminLayout from '../../components/AdminLayout.vue';
import { useToast } from '../../composables/toast';
import { api } from '../../api/client';
import { isOk } from '../../api/types';
import type { User } from '../../domain/types';
import { VERIFY_STATUS_LABEL } from '../../domain/types';
import { campusName } from '../../domain/campus';

const toast = useToast();
const users = ref<User[]>([]);
const keyword = ref('');
const loading = ref(true);

async function load() {
  loading.value = true;
  const res = await api.adminListUsers(keyword.value || undefined);
  if (isOk(res)) users.value = res.data;
  loading.value = false;
}

async function toggleBan(u: User) {
  const next = u.status === 'BANNED' ? 'ACTIVE' : 'BANNED';
  const res = await api.adminSetUserStatus(u.id, next);
  if (isOk(res)) {
    toast.show(next === 'BANNED' ? '已封禁' : '已解封');
    await load();
  }
}

let t: ReturnType<typeof setTimeout>;
watch(keyword, () => {
  clearTimeout(t);
  t = setTimeout(load, 200);
});
onMounted(load);
</script>

<template>
  <AdminLayout title="用户管理">
    <div class="asearch">
      <input class="input" v-model="keyword" placeholder="搜索昵称 / 学号" />
    </div>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else class="alist">
      <div v-for="u in users" :key="u.id" class="arow">
        <span class="arow__avatar">{{ u.avatar }}</span>
        <div class="arow__main">
          <div class="arow__name">
            {{ u.nickname }}
            <span v-if="u.status === 'BANNED'" class="tag" style="background:#fee2e2;color:#b91c1c">已封禁</span>
          </div>
          <div class="arow__sub faint">
            {{ u.studentNo || '无学号' }} · {{ campusName(u.campus) }} ·
            <span :class="u.verifyStatus === 'VERIFIED' ? 'ok' : ''">{{ VERIFY_STATUS_LABEL[u.verifyStatus] }}</span>
          </div>
        </div>
        <button class="arow__act" :class="{ 'is-danger': u.status !== 'BANNED' }" @click="toggleBan(u)">
          {{ u.status === 'BANNED' ? '解封' : '封禁' }}
        </button>
      </div>
    </div>
  </AdminLayout>
</template>

<style scoped>
.asearch {
  padding: 10px 14px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}
.alist {
  background: var(--c-surface);
}
.arow {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--c-border);
}
.arow__avatar {
  font-size: 28px;
}
.arow__main {
  flex: 1;
  min-width: 0;
}
.arow__name {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.arow__sub {
  font-size: 12px;
  margin-top: 3px;
}
.arow__sub .ok {
  color: var(--c-primary-dark);
}
.arow__act {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 999px;
}
.arow__act.is-danger {
  border-color: #fecaca;
  color: var(--c-danger);
}

@media (min-width: 900px) {
  .asearch,
  .alist {
    border-radius: var(--radius);
  }
  .asearch {
    border-bottom: none;
    margin-bottom: 14px;
  }
  .alist {
    border: 1px solid var(--c-border);
    overflow: hidden;
  }
}
</style>
