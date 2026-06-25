import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5180,
    strictPort: true,
    // 联调：浏览器同源请求 /api/**，由 dev server 代理到网关，绕开 CORS。
    // 配合 VITE_API_BASE=/api 使用（见 .env.example）。
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, ''),
      },
    },
  },
})
