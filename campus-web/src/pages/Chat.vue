<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ProductThumb from '../components/ProductThumb.vue';
import { useAuthStore } from '../stores/auth';
import { useCatalogStore } from '../stores/catalog';
import { useChatStore } from '../stores/chat';
import { useToast } from '../composables/toast';
import { api } from '../api/client';
import { isOk, type SessionView } from '../api/types';
import type { Message, AppointmentStatus } from '../domain/types';
import { PRODUCT_STATUS_LABEL } from '../domain/types';
import { clockTime } from '../domain/format';
import { tradePlacesOf } from '../domain/campus';
import { QUICK_PHRASES, APPOINTMENT_TIME_PRESETS, SESSION_STATUS_DONE } from '../domain/chat';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const catalog = useCatalogStore();
const chat = useChatStore();
const toast = useToast();

const sessionId = route.params.id as string;
const session = ref<SessionView | null>(null);
const messages = ref<Message[]>([]);
const loading = ref(true);
const text = ref('');
const bodyRef = ref<HTMLElement | null>(null);

// 约交易合成器
const showAppt = ref(false);
const apptPlace = ref('');
const apptTime = ref('');

const me = computed(() => auth.user?.id ?? '');
const isSeller = computed(() => session.value?.sellerId === me.value);
const product = computed(() => session.value?.product ?? null);
const isDone = computed(() => (session.value ? SESSION_STATUS_DONE.includes(session.value.status) : false));
const canMarkSold = computed(
  () => isSeller.value && product.value != null && product.value.status !== 'SOLD' && product.value.status !== 'DELISTED',
);
const places = computed(() => (product.value ? tradePlacesOf(product.value.campus) : []));

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight;
  });
}

async function load() {
  const res = await api.getThread(sessionId, me.value);
  if (isOk(res)) {
    session.value = res.data.session;
    messages.value = res.data.messages;
    // 读取后未读清零，刷新底部角标
    await chat.load();
    scrollToBottom();
  }
  loading.value = false;
}

async function send(content: string) {
  const body = content.trim();
  if (!body || isDone.value) return;
  const res = await api.sendMessage(sessionId, me.value, { type: 'TEXT', content: body });
  if (isOk(res)) {
    messages.value.push(res.data);
    text.value = '';
    scrollToBottom();
  }
}

async function sendAppointment() {
  if (!apptPlace.value || !apptTime.value) {
    toast.show('选择地点和时间');
    return;
  }
  const res = await api.sendMessage(sessionId, me.value, {
    type: 'APPOINTMENT',
    content: '',
    appointment: { place: apptPlace.value, time: apptTime.value, status: 'PROPOSED' },
  });
  if (isOk(res)) {
    messages.value.push(res.data);
    showAppt.value = false;
    apptPlace.value = '';
    apptTime.value = '';
    scrollToBottom();
  }
}

async function respond(msg: Message, status: AppointmentStatus) {
  const res = await api.respondAppointment(msg.id, status);
  if (isOk(res)) {
    const i = messages.value.findIndex((m) => m.id === msg.id);
    if (i >= 0) messages.value[i] = res.data;
    toast.show(status === 'CONFIRMED' ? '已确认交易地点' : '已取消约定');
  }
}

async function markSold() {
  const res = await api.markSoldInSession(sessionId, me.value);
  if (isOk(res)) {
    session.value = res.data;
    await chat.load();
    toast.show('已标记成交，交易完成 🎉');
  } else {
    toast.show(res.message);
  }
}

const apptStatusLabel: Record<AppointmentStatus, string> = {
  PROPOSED: '等待对方确认',
  CONFIRMED: '已确认',
  CANCELLED: '已取消',
};

onMounted(load);
</script>

<template>
  <div class="chat">
    <header class="topbar">
      <button class="topbar__back" @click="router.back()">‹</button>
      <span class="topbar__title">{{ session?.counterpart.nickname ?? '聊天' }}</span>
      <button v-if="canMarkSold" class="chat-sold" @click="markSold">标记已售出</button>
    </header>

    <!-- 商品卡 -->
    <div v-if="product" class="chat-product" @click="router.push(`/product/${product.id}`)">
      <div class="chat-product__thumb">
        <ProductThumb :seed="product.id" :emoji="catalog.categoryIcon(product.categoryId)" rounded />
      </div>
      <div class="chat-product__info">
        <div class="chat-product__title">{{ product.title }}</div>
        <div class="price"><span class="price__symbol">¥</span>{{ product.price }}</div>
      </div>
      <span class="chat-product__status">{{ PRODUCT_STATUS_LABEL[product.status] }}</span>
    </div>

    <!-- 消息区 -->
    <div ref="bodyRef" class="chat-body">
      <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
      <template v-else>
        <div v-for="m in messages" :key="m.id">
          <!-- 约交易卡片 -->
          <div v-if="m.type === 'APPOINTMENT' && m.appointment" class="appt">
            <div class="appt__title">📅 校园交易约定</div>
            <div class="appt__row">交易地点：{{ m.appointment.place }}</div>
            <div class="appt__row">时间：{{ m.appointment.time }}</div>
            <div class="appt__status" :class="`st-${m.appointment.status}`">
              状态：{{ apptStatusLabel[m.appointment.status] }}
            </div>
            <div v-if="m.appointment.status === 'PROPOSED' && m.senderId !== me && !isDone" class="appt__actions">
              <button class="btn btn--primary" @click="respond(m, 'CONFIRMED')">确认</button>
              <button class="btn btn--ghost" @click="respond(m, 'CANCELLED')">不方便</button>
            </div>
          </div>
          <!-- 普通气泡 -->
          <div v-else class="bubble-row" :class="{ 'is-me': m.senderId === me }">
            <div class="bubble">{{ m.content }}</div>
            <div class="bubble-time faint">{{ clockTime(m.createdAt) }}</div>
          </div>
        </div>
        <div v-if="isDone" class="chat-systip">该交易已完成</div>
      </template>
    </div>

    <!-- 输入区 -->
    <div v-if="!isDone" class="chat-input">
      <!-- 约交易合成器 -->
      <div v-if="showAppt" class="appt-composer">
        <div class="appt-composer__label">选择交易地点</div>
        <div class="appt-composer__chips">
          <button
            v-for="p in places"
            :key="p"
            class="chip"
            :class="{ 'is-active': apptPlace === p }"
            @click="apptPlace = p"
          >
            📍 {{ p }}
          </button>
        </div>
        <div class="appt-composer__label">选择时间</div>
        <div class="appt-composer__chips">
          <button
            v-for="t in APPOINTMENT_TIME_PRESETS"
            :key="t"
            class="chip"
            :class="{ 'is-active': apptTime === t }"
            @click="apptTime = t"
          >
            {{ t }}
          </button>
        </div>
        <button class="btn btn--primary btn--block" style="margin-top:10px" @click="sendAppointment">发送约定</button>
      </div>

      <!-- 快捷短语 -->
      <div v-else class="quick">
        <button v-for="q in QUICK_PHRASES" :key="q" class="quick__chip" @click="send(q)">{{ q }}</button>
      </div>

      <div class="chat-inputbar">
        <button class="chat-appt-btn" :class="{ 'is-on': showAppt }" @click="showAppt = !showAppt">📅</button>
        <input
          class="chat-text"
          v-model="text"
          placeholder="发消息…"
          @keyup.enter="send(text)"
        />
        <button class="btn btn--primary chat-send" :disabled="!text.trim()" @click="send(text)">发送</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.chat-sold {
  margin-left: auto;
  border: none;
  background: var(--c-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
}
.chat-product {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
}
.chat-product__thumb {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}
.chat-product__info {
  flex: 1;
  min-width: 0;
}
.chat-product__title {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chat-product__status {
  font-size: 11px;
  color: var(--c-text-soft);
  background: var(--c-surface-2);
  padding: 2px 8px;
  border-radius: 6px;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding: 14px 12px;
  background: var(--c-bg);
}
.bubble-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 14px;
}
.bubble-row.is-me {
  align-items: flex-end;
}
.bubble {
  max-width: 75%;
  padding: 9px 13px;
  border-radius: 14px;
  background: var(--c-surface);
  font-size: 14px;
  line-height: 1.5;
  box-shadow: var(--shadow-card);
}
.bubble-row.is-me .bubble {
  background: var(--c-primary);
  color: #fff;
}
.bubble-time {
  font-size: 10px;
  margin-top: 3px;
}
.appt {
  background: var(--c-surface);
  border: 1px solid var(--c-primary-soft);
  border-radius: var(--radius);
  padding: 12px 14px;
  margin: 0 auto 14px;
  max-width: 80%;
  box-shadow: var(--shadow-card);
}
.appt__title {
  font-weight: 700;
  font-size: 14px;
  margin-bottom: 8px;
}
.appt__row {
  font-size: 13px;
  color: var(--c-text);
  margin-bottom: 4px;
}
.appt__status {
  font-size: 12px;
  margin-top: 6px;
  font-weight: 600;
}
.appt__status.st-PROPOSED {
  color: var(--c-warning);
}
.appt__status.st-CONFIRMED {
  color: var(--c-primary-dark);
}
.appt__status.st-CANCELLED {
  color: var(--c-text-faint);
}
.appt__actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.appt__actions .btn {
  flex: 1;
  padding: 8px;
  font-size: 14px;
}
.chat-systip {
  text-align: center;
  font-size: 12px;
  color: var(--c-text-faint);
  margin: 10px 0;
}
.chat-input {
  background: var(--c-surface);
  border-top: 1px solid var(--c-border);
  flex-shrink: 0;
}
.quick {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 8px 12px;
}
.quick__chip {
  flex-shrink: 0;
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  white-space: nowrap;
}
.appt-composer {
  padding: 12px;
}
.appt-composer__label {
  font-size: 12px;
  color: var(--c-text-soft);
  margin: 6px 0;
}
.appt-composer__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 999px;
}
.chip.is-active {
  border-color: var(--c-primary);
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
  font-weight: 600;
}
.chat-inputbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
}
.chat-appt-btn {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  width: 38px;
  height: 38px;
  border-radius: 50%;
  font-size: 18px;
  flex-shrink: 0;
}
.chat-appt-btn.is-on {
  background: var(--c-primary-soft);
  border-color: var(--c-primary);
}
.chat-text {
  flex: 1;
  border: 1px solid var(--c-border);
  border-radius: 999px;
  padding: 9px 14px;
  font-size: 14px;
  outline: none;
}
.chat-text:focus {
  border-color: var(--c-primary);
}
.chat-send {
  flex-shrink: 0;
  padding: 9px 16px;
}
</style>
