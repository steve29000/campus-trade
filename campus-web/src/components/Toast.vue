<script setup lang="ts">
import { useToast } from '../composables/toast';
const { message } = useToast();
</script>

<template>
  <transition name="toast">
    <div v-if="message" class="toast">{{ message }}</div>
  </transition>
</template>

<style scoped>
.toast {
  position: fixed;
  left: 50%;
  bottom: 90px;
  transform: translateX(-50%);
  background: rgba(17, 24, 39, 0.92);
  color: #fff;
  font-size: 13px;
  padding: 10px 16px;
  border-radius: 999px;
  z-index: 100;
  max-width: 80%;
  text-align: center;
}
.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}
</style>
