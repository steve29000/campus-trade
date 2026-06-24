<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import ProductThumb from '../components/ProductThumb.vue';
import { useChatStore } from '../stores/chat';
import { useCatalogStore } from '../stores/catalog';
import { timeAgo } from '../domain/format';
import { SESSION_STATUS_DONE } from '../domain/chat';

const router = useRouter();
const chat = useChatStore();
const catalog = useCatalogStore();

onMounted(() => chat.load());
</script>

<template>
  <div>
    <header class="topbar">
      <span class="topbar__title">消息</span>
    </header>

    <div v-if="chat.sessions.length === 0" class="empty" style="padding-top:90px">
      <div class="empty__emoji">💬</div>
      <div class="empty__text">还没有消息，去商品详情「聊一聊」吧</div>
    </div>

    <div v-else class="msg-list">
      <div v-for="s in chat.sessions" :key="s.id" class="msg-row" @click="router.push(`/chat/${s.id}`)">
        <div class="msg-avatar">{{ s.counterpart.avatar }}</div>
        <div class="msg-main">
          <div class="msg-line1">
            <span class="msg-name">{{ s.counterpart.nickname }}</span>
            <span class="msg-time faint">{{ timeAgo(s.lastMessageTime) }}</span>
          </div>
          <div class="msg-line2">
            <span class="msg-last">{{ s.lastMessage || '开始聊聊吧～' }}</span>
            <span v-if="s.myUnread > 0" class="msg-unread">{{ s.myUnread }}</span>
          </div>
        </div>
        <div class="msg-thumb">
          <ProductThumb v-if="s.product" :seed="s.product.id" :emoji="catalog.categoryIcon(s.product.categoryId)" rounded />
          <span v-if="SESSION_STATUS_DONE.includes(s.status)" class="msg-done">已成交</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.msg-list {
  background: var(--c-surface);
}
.msg-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--c-border);
}
.msg-avatar {
  font-size: 30px;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--c-surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.msg-main {
  flex: 1;
  min-width: 0;
}
.msg-line1 {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.msg-name {
  font-size: 15px;
  font-weight: 600;
}
.msg-time {
  font-size: 11px;
}
.msg-line2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 3px;
}
.msg-last {
  flex: 1;
  font-size: 13px;
  color: var(--c-text-soft);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.msg-unread {
  flex-shrink: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--c-price);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}
.msg-thumb {
  position: relative;
  width: 46px;
  height: 46px;
  flex-shrink: 0;
}
.msg-done {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius);
}
</style>
