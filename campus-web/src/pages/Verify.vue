<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useToast } from '../composables/toast';
import { api } from '../api/client';
import { isOk } from '../api/types';
import { CAMPUSES } from '../domain/campus';
import type { CampusId } from '../domain/types';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const form = ref({
  school: auth.user?.school || '示范大学',
  campus: (auth.user?.campus || 'SOUTH') as CampusId,
  studentNo: auth.user?.studentNo || '',
});
const submitting = ref(false);
const error = ref('');

async function submit() {
  error.value = '';
  if (!form.value.studentNo.trim()) {
    error.value = '请填写学号';
    return;
  }
  submitting.value = true;
  const msg = await auth.submitVerification({ ...form.value, studentNo: form.value.studentNo.trim() });
  submitting.value = false;
  if (msg) error.value = msg;
  else toast.show('认证已提交，等待审核');
}

// 演示用：模拟管理员通过（真实由后台「认证审核」页处理）
async function demoApprove() {
  const v = await api.reviewLatestVerification(auth.user!.id, true);
  if (isOk(v)) {
    await auth.refresh();
    toast.show('已通过认证（演示）');
    router.replace((route.query.redirect as string) || '/me');
  }
}

function done() {
  router.replace((route.query.redirect as string) || '/me');
}
</script>

<template>
  <div>
    <header class="topbar">
      <button class="topbar__back" @click="router.back()">‹</button>
      <span class="topbar__title">校园认证</span>
    </header>

    <!-- 已认证 -->
    <div v-if="auth.user?.verifyStatus === 'VERIFIED'" class="vf-state">
      <div class="vf-state__emoji">✅</div>
      <h2>已完成校园认证</h2>
      <p class="faint">你可以发布闲置、收藏和联系卖家了。</p>
      <button class="btn btn--primary btn--lg" @click="done">完成</button>
    </div>

    <!-- 审核中 -->
    <div v-else-if="auth.user?.verifyStatus === 'PENDING'" class="vf-state">
      <div class="vf-state__emoji">⏳</div>
      <h2>认证审核中</h2>
      <p class="faint">平台正在核对你的学籍信息，通常很快。</p>
      <button class="btn btn--primary btn--lg" @click="demoApprove">🧪 演示：模拟管理员通过</button>
      <p class="faint" style="font-size:12px;margin-top:10px">真实环境由后台「认证审核」处理</p>
    </div>

    <!-- 表单 -->
    <div v-else class="vf-form">
      <div class="vf-banner">
        <strong>为什么要认证？</strong>
        <p>校园集市只允许认证学生交易。绿色盾牌不是画上去的——认证后同学才敢和你交易。</p>
      </div>
      <div class="field">
        <label class="field__label">学校</label>
        <input class="input" v-model="form.school" />
      </div>
      <div class="field">
        <label class="field__label">校区</label>
        <select class="select" v-model="form.campus">
          <option v-for="c in CAMPUSES" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>
      <div class="field">
        <label class="field__label">学号</label>
        <input class="input" v-model="form.studentNo" placeholder="如 2021SOUTH001" />
      </div>
      <div class="field">
        <label class="field__label">学生证照片（可选）</label>
        <div class="vf-upload faint">📷 点击上传（演示版可跳过）</div>
      </div>
      <p v-if="error" class="vf-error">{{ error }}</p>
      <button class="btn btn--primary btn--block btn--lg" :disabled="submitting" @click="submit">
        <span v-if="submitting" class="spin"></span>
        <span v-else>提交认证</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.vf-state {
  text-align: center;
  padding: 70px 30px;
}
.vf-state__emoji {
  font-size: 56px;
}
.vf-state h2 {
  margin: 14px 0 6px;
}
.vf-state .btn {
  margin-top: 20px;
}
.vf-form {
  padding: 16px;
}
.vf-banner {
  background: var(--c-primary-soft);
  border-radius: var(--radius);
  padding: 14px;
  margin-bottom: 18px;
  font-size: 13px;
  color: var(--c-primary-dark);
}
.vf-banner p {
  margin: 6px 0 0;
  line-height: 1.6;
}
.vf-upload {
  border: 1px dashed var(--c-border);
  border-radius: var(--radius-sm);
  padding: 18px;
  text-align: center;
  font-size: 13px;
}
.vf-error {
  color: var(--c-danger);
  font-size: 13px;
  text-align: center;
}
</style>
