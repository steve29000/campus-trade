import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { api } from '../api/client';
import { isOk, type SessionView } from '../api/types';
import { useAuthStore } from './auth';

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<SessionView[]>([]);

  const totalUnread = computed(() => sessions.value.reduce((sum, s) => sum + s.myUnread, 0));

  async function load() {
    const auth = useAuthStore();
    if (!auth.user) {
      sessions.value = [];
      return;
    }
    const res = await api.listSessions(auth.user.id);
    if (isOk(res)) sessions.value = res.data;
  }

  function clear() {
    sessions.value = [];
  }

  return { sessions, totalUnread, load, clear };
});
