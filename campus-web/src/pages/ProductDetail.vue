<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ProductThumb from '../components/ProductThumb.vue';
import { useAuthStore } from '../stores/auth';
import { useFavoriteStore } from '../stores/favorites';
import { useCatalogStore } from '../stores/catalog';
import { useToast } from '../composables/toast';
import { api } from '../api/client';
import { isOk, type ProductDetail } from '../api/types';
import { CONDITION_LABEL, PRODUCT_STATUS_LABEL, REPORT_REASONS, type ReportReason } from '../domain/types';
import { campusName, tradePlacesOf, pseudoDistanceKm, formatDistance } from '../domain/campus';
import { timeAgo } from '../domain/format';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const favorites = useFavoriteStore();
const catalog = useCatalogStore();
const toast = useToast();

const detail = ref<ProductDetail | null>(null);
const loading = ref(true);
const notFound = ref(false);
const showReport = ref(false);

const id = route.params.id as string;
const faved = computed(() => favorites.has(id));
const isMine = computed(() => detail.value?.sellerId === auth.user?.id);
const distance = computed(() =>
  detail.value ? formatDistance(pseudoDistanceKm(auth.viewerCampus, detail.value)) : '',
);

onMounted(async () => {
  const res = await api.getProduct(id);
  if (isOk(res) && res.data) detail.value = res.data;
  else notFound.value = true;
  loading.value = false;
});

async function toggleFav() {
  const r = await favorites.toggle(id);
  if (r === null) return router.push({ name: 'login', query: { redirect: route.fullPath } });
  toast.show(r ? '已收藏' : '已取消收藏');
}

async function contact() {
  if (!auth.isAuthed) return router.push({ name: 'login', query: { redirect: route.fullPath } });
  if (!auth.isVerified) return router.push('/verify');
  if (isMine.value) return toast.show('这是你发布的商品');
  const res = await api.startSession(id, auth.user!.id);
  if (isOk(res)) router.push(`/chat/${res.data.id}`);
  else toast.show(res.message);
}

async function report(reason: ReportReason) {
  showReport.value = false;
  if (!auth.isAuthed) return router.push({ name: 'login', query: { redirect: route.fullPath } });
  await api.submitReport({ reporterId: auth.user!.id, targetType: 'PRODUCT', targetId: id, reason });
  toast.show('举报已提交，平台会尽快处理');
}
</script>

<template>
  <div>
    <header class="topbar">
      <button class="topbar__back" @click="router.back()">‹</button>
      <span class="topbar__title">商品详情</span>
    </header>

    <div v-if="loading" class="empty"><span class="spin" style="border-color:#ddd;border-top-color:var(--c-primary)"></span></div>
    <div v-else-if="notFound" class="empty">
      <div class="empty__emoji">🫥</div>
      <div class="empty__text">商品不存在或已被删除</div>
    </div>

    <div v-else-if="detail" class="pd">
      <div class="pd-layout">
        <!-- 图片轮播 -->
        <div class="pd-gallery">
          <div v-for="(im, i) in detail.images" :key="i" class="pd-gallery__item">
            <ProductThumb :seed="detail.id" :index="i" :emoji="catalog.categoryIcon(detail.categoryId)" :src="im.url || undefined" />
          </div>
        </div>

        <div class="pd-main">
          <!-- 价格 / 标题 -->
          <div class="pd-head card">
            <div class="pd-price">
              <span class="price" style="font-size:26px"><span class="price__symbol">¥</span>{{ detail.price }}</span>
              <span v-if="detail.originalPrice" class="price__origin">原价 ¥{{ detail.originalPrice }}</span>
              <span v-if="detail.negotiable" class="tag" style="margin-left:8px">可议价</span>
              <span v-if="detail.status !== 'ON_SALE'" class="tag" style="margin-left:auto">{{ PRODUCT_STATUS_LABEL[detail.status] }}</span>
            </div>
            <h1 class="pd-title">{{ detail.title }}</h1>
            <div class="pd-meta faint">
              <span class="tag tag--condition">{{ CONDITION_LABEL[detail.conditionLevel] }}</span>
              <span>{{ timeAgo(detail.createdAt) }}发布</span>
              <span>· {{ detail.viewCount }} 浏览</span>
              <span>· {{ detail.favoriteCount }} 想要</span>
            </div>
          </div>

          <!-- 描述 -->
          <div class="pd-sec card">
            <div class="pd-sec__title">商品描述</div>
            <p class="pd-desc">{{ detail.description }}</p>
          </div>

          <!-- 校园交易地点（特色） -->
          <div class="pd-sec card">
            <div class="pd-sec__title">📍 校园交易</div>
            <div class="pd-place__main">{{ campusName(detail.campus) }} · 距你约 {{ distance }}</div>
            <div class="pd-place__sug faint">卖家建议：{{ detail.locationDesc }}</div>
            <div class="pd-place__chips">
              <span class="tag" v-for="pl in tradePlacesOf(detail.campus)" :key="pl">{{ pl }}</span>
            </div>
            <div class="pd-place__tip">平台建议在以上人多、明亮的地点当面交易，安全放心。</div>
          </div>

          <!-- 卖家 -->
          <div class="pd-seller card">
            <span class="pd-seller__avatar">{{ detail.seller.avatar }}</span>
            <div class="pd-seller__info">
              <div class="pd-seller__name">
                {{ detail.seller.nickname }}
                <span v-if="detail.seller.verifyStatus === 'VERIFIED'" class="tag tag--verified">✓ 校园认证</span>
              </div>
              <div class="faint" style="font-size:12px">信用 {{ detail.seller.rating }} · {{ campusName(detail.seller.campus) }}</div>
            </div>
            <button class="pd-report" @click="auth.isAuthed ? (showReport = true) : router.push({ name: 'login', query: { redirect: route.fullPath } })">举报</button>
          </div>

          <!-- 操作栏 -->
          <div class="pd-bar">
            <button class="pd-bar__icon" @click="toggleFav">
              <span>{{ faved ? '❤️' : '🤍' }}</span><span>收藏</span>
            </button>
            <button
              class="btn btn--primary pd-bar__chat"
              :disabled="detail.status !== 'ON_SALE' && !isMine"
              @click="contact"
            >
              {{ isMine ? '这是我发布的' : detail.status === 'ON_SALE' ? '聊一聊' : '已不可交易' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 举报选项 -->
    <div v-if="showReport" class="sheet-mask" @click.self="showReport = false">
      <div class="sheet">
        <div class="sheet__title">举报该商品</div>
        <button v-for="r in REPORT_REASONS" :key="r" class="sheet__item" @click="report(r)">{{ r }}</button>
        <button class="sheet__cancel" @click="showReport = false">取消</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pd {
  padding-bottom: 76px;
}
.pd-gallery {
  display: flex;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  background: #fff;
}
.pd-gallery__item {
  flex: 0 0 100%;
  scroll-snap-align: start;
}
.pd-head {
  margin: 10px;
  padding: 14px;
}
.pd-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.pd-title {
  margin: 8px 0 8px;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
}
.pd-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  flex-wrap: wrap;
}
.pd-sec {
  margin: 10px;
  padding: 14px;
}
.pd-sec__title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}
.pd-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #374151;
}
.pd-place__main {
  font-size: 14px;
  font-weight: 500;
}
.pd-place__sug {
  font-size: 13px;
  margin: 4px 0 10px;
}
.pd-place__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.pd-place__tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--c-primary-dark);
  background: var(--c-primary-soft);
  padding: 8px 10px;
  border-radius: var(--radius-sm);
}
.pd-seller {
  margin: 10px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.pd-seller__avatar {
  font-size: 30px;
}
.pd-seller__info {
  flex: 1;
}
.pd-seller__name {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.pd-report {
  border: 1px solid var(--c-border);
  background: none;
  color: var(--c-text-faint);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 999px;
}
.pd-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: var(--frame-w);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  background: var(--c-surface);
  border-top: 1px solid var(--c-border);
  z-index: 20;
}
.pd-bar__icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 11px;
  gap: 1px;
  background: none;
  border: none;
  color: var(--c-text-soft);
}
.pd-bar__icon span:first-child {
  font-size: 20px;
}
.pd-bar__chat {
  flex: 1;
}
.sheet-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 50;
  display: flex;
  align-items: flex-end;
}
.sheet {
  width: 100%;
  max-width: var(--frame-w);
  margin: 0 auto;
  background: var(--c-surface);
  border-radius: 16px 16px 0 0;
  padding: 8px 0;
}
.sheet__title {
  text-align: center;
  font-size: 13px;
  color: var(--c-text-faint);
  padding: 10px;
}
.sheet__item {
  width: 100%;
  border: none;
  background: none;
  padding: 14px;
  font-size: 15px;
  border-top: 1px solid var(--c-border);
}
.sheet__cancel {
  width: 100%;
  border: none;
  background: var(--c-surface-2);
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 6px;
}

/* ---------- 桌面：左图右信息两栏 ---------- */
@media (min-width: 900px) {
  .pd {
    padding-bottom: 0;
  }
  .pd-layout {
    display: flex;
    align-items: flex-start;
    gap: 24px;
    max-width: 1180px;
    margin: 0 auto;
    padding: 20px;
  }
  .pd-gallery {
    flex: 0 0 460px;
    width: 460px;
    position: sticky;
    top: 82px;
    border-radius: var(--radius);
    overflow-x: auto;
    box-shadow: var(--shadow-card);
  }
  .pd-main {
    flex: 1;
    min-width: 0;
  }
  .pd-main .pd-head,
  .pd-main .pd-sec,
  .pd-main .pd-seller {
    margin: 0 0 14px;
  }
  .pd-bar {
    position: static;
    transform: none;
    width: auto;
    max-width: none;
    border-top: none;
    background: none;
    padding: 4px 0 0;
  }
  .pd-bar__chat {
    padding-top: 13px;
    padding-bottom: 13px;
    font-size: 16px;
  }
}
</style>
