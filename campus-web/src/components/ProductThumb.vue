<script setup lang="ts">
import { computed } from 'vue';

// 离线安全的占位图：用 seed 派生稳定渐变，居中显示分类 emoji。
// 真实上传图片时传 src（blob/http），优先展示真实图片。
const props = withDefaults(
  defineProps<{ seed: string; emoji?: string; src?: string; index?: number; rounded?: boolean }>(),
  { emoji: '📦', index: 0, rounded: false },
);

function hash(s: string): number {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  return Math.abs(h);
}

const gradient = computed(() => {
  const h = (hash(props.seed) + props.index * 47) % 360;
  const h2 = (h + 40) % 360;
  return `linear-gradient(135deg, hsl(${h} 62% 92%), hsl(${h2} 70% 84%))`;
});
</script>

<template>
  <div class="thumb" :class="{ 'thumb--rounded': rounded }" :style="{ background: src ? undefined : gradient }">
    <img v-if="src" :src="src" alt="" class="thumb__img" />
    <span v-else class="thumb__emoji">{{ emoji }}</span>
  </div>
</template>

<style scoped>
.thumb {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.thumb--rounded {
  border-radius: var(--radius);
}
.thumb__emoji {
  font-size: 44px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.08));
}
.thumb__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
