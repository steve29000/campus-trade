import { createApp } from 'vue';
import { createPinia } from 'pinia';
import './style.css';
import App from './App.vue';
import { router } from './router';
import { useAuthStore } from './stores/auth';
import { useFavoriteStore } from './stores/favorites';
import { useCatalogStore } from './stores/catalog';
import { useChatStore } from './stores/chat';

async function bootstrap() {
  const app = createApp(App);
  app.use(createPinia());
  app.use(router);

  // 启动时恢复登录态，预加载分类与收藏，再挂载，避免首屏闪烁。
  const auth = useAuthStore();
  await auth.restore();
  await Promise.all([useCatalogStore().load(), useFavoriteStore().load(), useChatStore().load()]);

  app.mount('#app');
}

bootstrap();
