import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useHttp } from '../api/config';

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../pages/Login.vue') },
  { path: '/verify', name: 'verify', component: () => import('../pages/Verify.vue'), meta: { requiresAuth: true } },

  { path: '/', name: 'home', component: () => import('../pages/Home.vue'), meta: { tab: 'home' } },
  { path: '/search', name: 'search', component: () => import('../pages/Search.vue'), meta: { tab: 'search' } },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('../pages/Publish.vue'),
    meta: { tab: 'publish', requiresAuth: true },
  },
  {
    path: '/messages',
    name: 'messages',
    component: () => import('../pages/Messages.vue'),
    meta: { tab: 'messages', requiresAuth: true },
  },
  { path: '/me', name: 'me', component: () => import('../pages/Profile.vue'), meta: { tab: 'me' } },

  { path: '/product/:id', name: 'product', component: () => import('../pages/ProductDetail.vue') },
  { path: '/favorites', name: 'favorites', component: () => import('../pages/Favorites.vue'), meta: { requiresAuth: true } },
  { path: '/my-listings', name: 'my-listings', component: () => import('../pages/MyListings.vue'), meta: { requiresAuth: true } },
  { path: '/chat/:id', name: 'chat', component: () => import('../pages/Chat.vue'), meta: { requiresAuth: true } },

  // 后台管理
  { path: '/admin/login', name: 'admin-login', component: () => import('../pages/admin/AdminLogin.vue') },
  { path: '/admin', name: 'admin', component: () => import('../pages/admin/AdminDashboard.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/users', name: 'admin-users', component: () => import('../pages/admin/AdminUsers.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/products', name: 'admin-products', component: () => import('../pages/admin/AdminProducts.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/verifications', name: 'admin-verifications', component: () => import('../pages/admin/AdminVerifications.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/reports', name: 'admin-reports', component: () => import('../pages/admin/AdminReports.vue'), meta: { requiresAdmin: true } },

  { path: '/:pathMatch(.*)*', redirect: '/' },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

// 不需要登录即可访问的页面（联调模式下也放行）
const AUTH_PAGES = new Set(['login', 'admin-login']);

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.requiresAdmin) {
    if (!auth.isAdmin) return { name: 'admin-login', query: { redirect: to.fullPath } };
    return true;
  }
  // 联调（HTTP）模式：后端除登录/注册外全部鉴权，未登录统一引导先登录
  if (useHttp && !auth.isAuthed && !AUTH_PAGES.has(to.name as string)) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAuth && !auth.isAuthed) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  return true;
});
