import { ref } from 'vue';

// 模块级单例：全局轻提示。
const message = ref('');
let timer: ReturnType<typeof setTimeout> | undefined;

export function useToast() {
  function show(msg: string, ms = 1700) {
    message.value = msg;
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => (message.value = ''), ms);
  }
  return { message, show };
}
