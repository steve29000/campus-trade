import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { api } from '../api/client';
import { isOk } from '../api/types';
import type { User, CampusId } from '../domain/types';

const UID_KEY = 'campus-web-uid';

// 浏览校区：null 表示"全部校区"
export type BrowseCampus = CampusId | 'ALL';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null);
  const browseCampus = ref<BrowseCampus>('ALL');
  const loading = ref(false);

  const isAuthed = computed(() => user.value !== null);
  const isVerified = computed(() => user.value?.verifyStatus === 'VERIFIED');
  const isAdmin = computed(() => user.value?.role === 'ADMIN');
  // 用于"距离最近"等：当前用户的归属校区
  const viewerCampus = computed<CampusId>(() => user.value?.campus ?? 'SOUTH');

  function adopt(u: User) {
    user.value = u;
    browseCampus.value = u.campus;
    localStorage.setItem(UID_KEY, u.id);
  }

  async function restore() {
    const uid = localStorage.getItem(UID_KEY);
    if (!uid) return;
    const res = await api.me(uid);
    if (isOk(res) && res.data) adopt(res.data);
    else localStorage.removeItem(UID_KEY);
  }

  async function login(emailOrId: string): Promise<string | null> {
    loading.value = true;
    try {
      const res = await api.login(emailOrId);
      if (!isOk(res) || !res.data) return res.message;
      adopt(res.data);
      return null;
    } finally {
      loading.value = false;
    }
  }

  async function loginDemo(): Promise<void> {
    loading.value = true;
    try {
      const res = await api.loginDemo();
      if (isOk(res) && res.data) adopt(res.data);
    } finally {
      loading.value = false;
    }
  }

  async function register(payload: { nickname: string; email: string; school: string; campus: CampusId }): Promise<string | null> {
    loading.value = true;
    try {
      const res = await api.register(payload);
      if (!isOk(res) || !res.data) return res.message;
      adopt(res.data);
      return null;
    } finally {
      loading.value = false;
    }
  }

  async function submitVerification(payload: { school: string; campus: CampusId; studentNo: string; proofImage?: string }): Promise<string | null> {
    if (!user.value) return '请先登录';
    const res = await api.submitVerification(user.value.id, payload);
    if (!isOk(res)) return res.message;
    // 同步刷新本地用户（verifyStatus 已变为 PENDING）
    const me = await api.me(user.value.id);
    if (isOk(me) && me.data) user.value = me.data;
    return null;
  }

  async function refresh() {
    if (!user.value) return;
    const me = await api.me(user.value.id);
    if (isOk(me) && me.data) user.value = me.data;
  }

  function logout() {
    user.value = null;
    localStorage.removeItem(UID_KEY);
  }

  return {
    user,
    browseCampus,
    loading,
    isAuthed,
    isVerified,
    isAdmin,
    viewerCampus,
    restore,
    login,
    loginDemo,
    register,
    submitVerification,
    refresh,
    logout,
  };
});
