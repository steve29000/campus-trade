<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useFavoriteStore } from '../stores/favorites';
import { useChatStore } from '../stores/chat';
import { CAMPUSES } from '../domain/campus';
import type { CampusId } from '../domain/types';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const favorites = useFavoriteStore();
const chat = useChatStore();

const mode = ref<'login' | 'register'>('login');
const error = ref('');

const account = ref('');
const reg = ref({ nickname: '', email: '', campus: 'SOUTH' as CampusId, school: '示范大学' });

async function afterAuth() {
  await Promise.all([favorites.load(), chat.load()]);
  const redirect = (route.query.redirect as string) || '/';
  router.replace(redirect);
}

async function doLogin() {
  error.value = '';
  const msg = await auth.login(account.value.trim());
  if (msg) error.value = msg;
  else await afterAuth();
}

async function quickDemo(kind: 'verified' | 'unverified') {
  error.value = '';
  if (kind === 'verified') await auth.loginDemo();
  else await auth.login('u5');
  await afterAuth();
}

async function doRegister() {
  error.value = '';
  if (!reg.value.email.trim()) {
    error.value = '请填写邮箱';
    return;
  }
  const msg = await auth.register({ ...reg.value, email: reg.value.email.trim() });
  if (msg) error.value = msg;
  else await afterAuth();
}
</script>

<template>
  <div class="login">
    <div class="login__brand">
      <div class="login__logo">🛍️</div>
      <h1 class="login__name">校园集市</h1>
      <p class="login__slogan">认证学生的二手闲置 · 就近放心交易</p>
    </div>

    <div class="login__card card">
      <template v-if="mode === 'login'">
        <div class="field">
          <label class="field__label">邮箱 / 学号 / 演示账号</label>
          <input class="input" v-model="account" placeholder="如 nan@stu.edu.cn 或 u1" @keyup.enter="doLogin" />
        </div>
        <button class="btn btn--primary btn--block btn--lg" :disabled="auth.loading" @click="doLogin">
          <span v-if="auth.loading" class="spin"></span>
          <span v-else>登录</span>
        </button>
        <p class="login__switch">
          还没有账号？<a @click="mode = 'register'">去注册</a>
        </p>
      </template>

      <template v-else>
        <div class="field">
          <label class="field__label">昵称</label>
          <input class="input" v-model="reg.nickname" placeholder="给同学一个称呼" />
        </div>
        <div class="field">
          <label class="field__label">学生邮箱</label>
          <input class="input" v-model="reg.email" placeholder="xxx@stu.edu.cn" />
        </div>
        <div class="field">
          <label class="field__label">校区</label>
          <select class="select" v-model="reg.campus">
            <option v-for="c in CAMPUSES" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <button class="btn btn--primary btn--block btn--lg" :disabled="auth.loading" @click="doRegister">
          注册并进入
        </button>
        <p class="login__hint faint">注册后默认未认证，可浏览；发布/聊天前需完成校园认证。</p>
        <p class="login__switch">
          已有账号？<a @click="mode = 'login'">去登录</a>
        </p>
      </template>

      <p v-if="error" class="login__error">{{ error }}</p>
    </div>

    <div class="login__demo">
      <div class="login__demo-title">快速体验</div>
      <button class="btn btn--ghost btn--block" @click="quickDemo('verified')">
        🦊 已认证学生（小南 · 南校区）
      </button>
      <button class="btn btn--ghost btn--block" @click="quickDemo('unverified')">
        🐤 未认证新生（体验认证守卫）
      </button>
    </div>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  padding: 0 22px 30px;
  background: linear-gradient(160deg, var(--c-primary-soft), var(--c-bg) 40%);
}
.login__brand {
  text-align: center;
  padding: 56px 0 28px;
}
.login__logo {
  font-size: 56px;
}
.login__name {
  margin: 8px 0 4px;
  font-size: 26px;
  letter-spacing: 2px;
}
.login__slogan {
  margin: 0;
  color: var(--c-text-soft);
  font-size: 13px;
}
.login__card {
  padding: 20px 18px 16px;
}
.login__switch {
  text-align: center;
  font-size: 13px;
  color: var(--c-text-soft);
  margin: 14px 0 2px;
}
.login__switch a {
  color: var(--c-primary-dark);
  font-weight: 600;
}
.login__hint {
  font-size: 12px;
  margin: 10px 0 0;
  line-height: 1.5;
}
.login__error {
  margin: 12px 0 0;
  color: var(--c-danger);
  font-size: 13px;
  text-align: center;
}
.login__demo {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.login__demo-title {
  text-align: center;
  font-size: 12px;
  color: var(--c-text-faint);
  position: relative;
}
</style>
