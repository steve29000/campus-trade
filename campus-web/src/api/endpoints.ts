// 路由前缀与 campus-gateway 完全一致。mock client 当前不真正发请求，
// 但保留这些常量，将来换成 HTTP client 时直接复用，做到"对接即可用"。
export const GATEWAY_BASE = 'http://localhost:8080';

export const EP = {
  // campus-user
  login: '/user/login',
  register: '/user/register',
  me: '/user/me',
  verify: '/user/verify',
  // campus-product
  products: '/product/list',
  product: (id: string) => `/product/${id}`,
  productStatus: (id: string) => `/product/${id}/status`,
  myProducts: '/product/mine',
  categories: '/product/categories',
  favorites: '/product/favorites',
  // campus-message
  sessions: '/message/sessions',
  messages: (sessionId: string) => `/message/sessions/${sessionId}/messages`,
  // campus-order
  orders: '/order/list',
  // campus-ai
  aiPrice: '/ai/price/estimate',
  aiDescription: '/ai/description/optimize',
  // admin（经 gateway 鉴权后进入各服务后台接口）
  adminUsers: '/user/admin/users',
  adminProducts: '/product/admin/products',
  adminVerifications: '/user/admin/verifications',
  adminReports: '/product/admin/reports',
} as const;
