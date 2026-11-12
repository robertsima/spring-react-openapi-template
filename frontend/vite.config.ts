import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// In dev, Vite plays the role nginx plays in the container: /api and /websockets are
// proxied to the backend so the browser only ever talks to one origin (no CORS needed).
const backend = process.env.BACKEND_URL ?? 'http://localhost:8080'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'window', // sockjs-client expects a Node-style global
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: backend,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      '/websockets': {
        target: backend,
        changeOrigin: true,
        ws: true,
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler' // or "modern"
      }
    }
  }
})
