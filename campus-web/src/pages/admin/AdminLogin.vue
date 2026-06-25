<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const error = ref('');

async function loginAdmin() {
  error.value = '';
  const msg = await auth.login('admin1');
  if (msg) {
    error.value = msg;
    return;
  }
  if (!auth.isAdmin) {
    error.value = '该账号无后台权限';
    return;
  }
  router.replace((route.query.redirect as string) || '/admin');
}
</script>

<template>
  <div class="alogin">
    <div class="alogin__brand">
      <div class="alogin__logo">🛡️</div>
      <h1>校园集市 · 管理后台</h1>
      <p class="faint">违规商品、用户认证、举报处理</p>
    </div>
    <div class="alogin__card card">
      <p class="faint" style="font-size:13px;margin-top:0">演示后台使用内置管理员账号，一键进入。</p>
      <button class="btn btn--primary btn--block btn--lg" @click="loginAdmin">以管理员身份登录</button>
      <p v-if="error" class="alogin__error">{{ error }}</p>
      <button class="btn btn--ghost btn--block" style="margin-top:10px" @click="router.push('/')">返回学生端</button>
    </div>
  </div>
</template>

<style scoped>
.alogin {
  min-height: 100%;
  padding: 60px 22px;
  background: linear-gradient(160deg, #1f2937, #0f172a);
  color: #fff;
}
.alogin__brand {
  text-align: center;
  margin-bottom: 30px;
}
.alogin__logo {
  font-size: 52px;
}
.alogin__brand h1 {
  font-size: 22px;
  margin: 10px 0 6px;
}
.alogin__brand .faint {
  color: #94a3b8;
}
.alogin__card {
  padding: 20px 18px;
  color: var(--c-text);
}
.alogin__error {
  color: var(--c-danger);
  font-size: 13px;
  text-align: center;
  margin-bottom: 0;
}

/* ---------- 桌面：居中卡片 ---------- */
@media (min-width: 900px) {
  .alogin__brand,
  .alogin__card {
    max-width: 420px;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
