<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import ProductThumb from '../components/ProductThumb.vue';
import { useAuthStore } from '../stores/auth';
import { useCatalogStore } from '../stores/catalog';
import { useToast } from '../composables/toast';
import { api } from '../api/client';
import { isOk } from '../api/types';
import type { PriceEstimate } from '../domain/ai';
import { CONDITION_LABEL, type ConditionLevel } from '../domain/types';
import { tradePlacesOf } from '../domain/campus';

const router = useRouter();
const auth = useAuthStore();
const catalog = useCatalogStore();
const toast = useToast();

const step = ref(1);
const submitting = ref(false);

// 表单
const photos = ref<string[]>([]); // 占位图种子（演示版用占位图，离线安全）
const categoryId = ref('');
const title = ref('');
const price = ref<number | null>(null);
const originalPrice = ref<number | null>(null);
const conditionLevel = ref<ConditionLevel>('GOOD');
const description = ref('');
const locationDesc = ref('');
const negotiable = ref(true);

// AI
const estimate = ref<PriceEstimate | null>(null);
const aiPricing = ref(false);
const aiWriting = ref(false);

const conditions: ConditionLevel[] = ['NEW', 'ALMOST_NEW', 'GOOD', 'WORN'];
const places = computed(() => (auth.user ? tradePlacesOf(auth.user.campus) : []));
const selectedCategory = computed(() => catalog.categories.find((c) => c.id === categoryId.value));
const categoryEmoji = computed(() => selectedCategory.value?.icon ?? '📷');

const step1ok = computed(() => photos.value.length > 0 && categoryId.value !== '');
const step2ok = computed(() => title.value.trim() !== '' && (price.value ?? 0) > 0);

function addPhoto() {
  if (photos.value.length >= 6) return;
  photos.value.push(Math.random().toString(36).slice(2));
}
function removePhoto(i: number) {
  photos.value.splice(i, 1);
}

async function runEstimate() {
  if (!(originalPrice.value && originalPrice.value > 0)) {
    toast.show('先填写原价，AI 才好估价');
    return;
  }
  aiPricing.value = true;
  estimate.value = null;
  const res = await api.estimatePrice({
    originalPrice: originalPrice.value,
    conditionLevel: conditionLevel.value,
    categoryName: selectedCategory.value?.name ?? '闲置',
    categoryHot: selectedCategory.value?.hot,
  });
  aiPricing.value = false;
  if (isOk(res)) estimate.value = res.data;
}
function applyEstimate() {
  if (estimate.value) price.value = estimate.value.suggestedPrice;
}

async function runWrite() {
  if (!auth.user) return;
  aiWriting.value = true;
  const res = await api.generateDescription({
    title: title.value || selectedCategory.value?.name || '闲置好物',
    rawNotes: description.value,
    conditionLevel: conditionLevel.value,
    categoryName: selectedCategory.value?.name ?? '闲置',
    campus: auth.user.campus,
    tradePlace: locationDesc.value || undefined,
  });
  aiWriting.value = false;
  if (isOk(res)) description.value = res.data;
}

async function publish() {
  if (!auth.user) return;
  submitting.value = true;
  const res = await api.createProduct({
    sellerId: auth.user.id,
    title: title.value.trim(),
    description: description.value.trim() || title.value.trim(),
    categoryId: categoryId.value,
    price: price.value!,
    originalPrice: originalPrice.value ?? undefined,
    conditionLevel: conditionLevel.value,
    campus: auth.user.campus,
    locationDesc: locationDesc.value || places.value[0] || '校内当面交易',
    negotiable: negotiable.value,
    imageCount: photos.value.length,
  });
  submitting.value = false;
  if (isOk(res)) {
    toast.show('发布成功 🎉');
    router.replace('/my-listings');
  } else {
    toast.show(res.message);
  }
}
</script>

<template>
  <div>
    <header class="topbar">
      <span class="topbar__title">发布闲置</span>
    </header>

    <!-- 未认证守卫 -->
    <div v-if="!auth.isVerified" class="empty" style="padding-top:80px">
      <div class="empty__emoji">🎓</div>
      <div class="empty__text">完成校园认证后才能发布商品</div>
      <button class="btn btn--primary btn--lg" style="margin-top:16px" @click="router.push('/verify')">去认证</button>
    </div>

    <template v-else>
      <!-- 步骤指示 -->
      <div class="steps">
        <div v-for="n in 3" :key="n" class="steps__item" :class="{ 'is-on': step >= n }">
          <span class="steps__dot">{{ n }}</span>
          <span class="steps__label">{{ ['上传·分类', '价格·成色', '描述·发布'][n - 1] }}</span>
        </div>
      </div>

      <!-- Step 1 -->
      <div v-show="step === 1" class="pub">
        <div class="field__label">商品图片（{{ photos.length }}/6）</div>
        <div class="pub-photos">
          <div v-for="(p, i) in photos" :key="p" class="pub-photo">
            <ProductThumb :seed="p" :emoji="categoryEmoji" rounded />
            <button class="pub-photo__del" @click="removePhoto(i)">✕</button>
          </div>
          <button v-if="photos.length < 6" class="pub-photo pub-photo__add" @click="addPhoto">＋<span>添加图片</span></button>
        </div>
        <div class="faint" style="font-size:12px;margin:6px 0 18px">演示版使用占位图，真实版接入图片上传。</div>

        <div class="field__label">选择分类</div>
        <div class="pub-cats">
          <button
            v-for="c in catalog.categories"
            :key="c.id"
            class="pub-cat"
            :class="{ 'is-active': categoryId === c.id }"
            @click="categoryId = c.id"
          >
            <span style="font-size:22px">{{ c.icon }}</span>
            <span>{{ c.name }}</span>
          </button>
        </div>
      </div>

      <!-- Step 2 -->
      <div v-show="step === 2" class="pub">
        <div class="field">
          <label class="field__label">商品标题</label>
          <input class="input" v-model="title" placeholder="如：飞利浦护眼台灯 充电款" />
        </div>
        <div class="field">
          <label class="field__label">成色</label>
          <div class="pub-conds">
            <button
              v-for="c in conditions"
              :key="c"
              class="pub-cond"
              :class="{ 'is-active': conditionLevel === c }"
              @click="conditionLevel = c"
            >
              {{ CONDITION_LABEL[c] }}
            </button>
          </div>
        </div>
        <div class="field pub-prices">
          <div style="flex:1">
            <label class="field__label">原价（选填）</label>
            <input class="input" type="number" v-model.number="originalPrice" placeholder="¥" />
          </div>
          <div style="flex:1">
            <label class="field__label">售价</label>
            <input class="input" type="number" v-model.number="price" placeholder="¥" />
          </div>
        </div>

        <!-- AI 估价 -->
        <button class="ai-btn" :disabled="aiPricing" @click="runEstimate">
          <span v-if="aiPricing" class="spin" style="border-color:rgba(16,185,129,.3);border-top-color:var(--c-primary)"></span>
          <span>🤖 {{ aiPricing ? 'AI 估价中…' : 'AI 帮我估价' }}</span>
        </button>
        <div v-if="estimate" class="ai-result">
          <div class="ai-result__range">
            建议 <strong class="price">¥{{ estimate.suggestedMin }}-{{ estimate.suggestedMax }}</strong>
            · 推荐 <strong class="price">¥{{ estimate.suggestedPrice }}</strong>
          </div>
          <div class="ai-result__reason faint">{{ estimate.reason }}</div>
          <button class="btn btn--primary btn--block" style="margin-top:8px" @click="applyEstimate">使用推荐价 ¥{{ estimate.suggestedPrice }}</button>
        </div>
      </div>

      <!-- Step 3 -->
      <div v-show="step === 3" class="pub">
        <div class="field">
          <label class="field__label">商品描述</label>
          <textarea class="textarea" v-model="description" placeholder="随手写几个字，例如：有机化学教材，八成新，有笔记"></textarea>
          <button class="ai-btn" style="margin-top:8px" :disabled="aiWriting" @click="runWrite">
            <span v-if="aiWriting" class="spin" style="border-color:rgba(16,185,129,.3);border-top-color:var(--c-primary)"></span>
            <span>✨ {{ aiWriting ? 'AI 生成中…' : 'AI 帮我写文案' }}</span>
          </button>
        </div>
        <div class="field">
          <label class="field__label">交易地点</label>
          <div class="pub-places">
            <button
              v-for="pl in places"
              :key="pl"
              class="pub-place"
              :class="{ 'is-active': locationDesc === pl }"
              @click="locationDesc = pl"
            >
              📍 {{ pl }}
            </button>
          </div>
        </div>
        <label class="pub-neg">
          <input type="checkbox" v-model="negotiable" />
          <span>可议价</span>
        </label>
      </div>

      <!-- 底部导航按钮 -->
      <div class="pub-bar">
        <button v-if="step > 1" class="btn btn--ghost" @click="step--">上一步</button>
        <button v-if="step === 1" class="btn btn--primary btn--block" :disabled="!step1ok" @click="step = 2">下一步</button>
        <button v-else-if="step === 2" class="btn btn--primary btn--block" :disabled="!step2ok" @click="step = 3">下一步</button>
        <button v-else class="btn btn--primary btn--block" :disabled="submitting" @click="publish">
          <span v-if="submitting" class="spin"></span>
          <span v-else>确认发布</span>
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.steps {
  display: flex;
  justify-content: space-around;
  padding: 16px 12px;
  background: var(--c-surface);
}
.steps__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--c-text-faint);
  font-size: 12px;
}
.steps__item.is-on {
  color: var(--c-primary);
}
.steps__dot {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--c-surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.steps__item.is-on .steps__dot {
  background: var(--c-primary);
  color: #fff;
}
.pub {
  padding: 16px;
}
.pub-photos {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.pub-photo {
  position: relative;
  aspect-ratio: 1;
}
.pub-photo__del {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 11px;
  line-height: 20px;
  padding: 0;
}
.pub-photo__add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border: 1px dashed var(--c-border);
  border-radius: var(--radius);
  background: var(--c-surface-2);
  color: var(--c-text-faint);
  font-size: 22px;
}
.pub-photo__add span {
  font-size: 11px;
}
.pub-cats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.pub-cat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 0;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  font-size: 12px;
  color: var(--c-text-soft);
}
.pub-cat.is-active {
  border-color: var(--c-primary);
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
}
.pub-conds,
.pub-places {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pub-cond,
.pub-place {
  border: 1px solid var(--c-border);
  background: var(--c-surface);
  color: var(--c-text-soft);
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
}
.pub-cond.is-active,
.pub-place.is-active {
  border-color: var(--c-primary);
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
  font-weight: 600;
}
.pub-prices {
  display: flex;
  gap: 12px;
}
.ai-btn {
  width: 100%;
  border: 1px solid var(--c-primary);
  background: var(--c-primary-soft);
  color: var(--c-primary-dark);
  font-weight: 600;
  font-size: 14px;
  padding: 11px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.ai-btn:disabled {
  opacity: 0.7;
}
.ai-result {
  margin-top: 12px;
  padding: 14px;
  background: var(--c-surface);
  border: 1px solid var(--c-primary-soft);
  border-radius: var(--radius);
}
.ai-result__range {
  font-size: 15px;
}
.ai-result__reason {
  font-size: 12px;
  line-height: 1.6;
  margin-top: 6px;
}
.pub-neg {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-top: 8px;
}
.pub-bar {
  display: flex;
  gap: 12px;
  padding: 14px;
}
</style>
