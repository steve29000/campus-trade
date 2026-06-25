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

  // 必须先恢复登录态，再安装 router。
  // 否则 app.use(router) 会立即触发首次路由解析与守卫，此时 restore 尚未完成，
  // 受保护路由（/publish、/my-listings）会误判未登录而跳到 /login。
  const auth = useAuthStore();
  await auth.restore();
  await Promise.all([useCatalogStore().load(), useFavoriteStore().load(), useChatStore().load()]);

  app.use(router);
  app.mount('#app');
}

bootstrap();
