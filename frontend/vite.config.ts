/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// In dev, Vite plays the role nginx plays in the container: /api and /websockets are
// proxied to the backend so the browser only ever talks to one origin (no CORS needed).
// Run the backend with SPRING_PROFILES_ACTIVE=<db>,localdev unless you're on a domain with Kerberos set up.
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
  },
  // Vitest: two projects so unit and integration suites can run separately (npm run test:unit / test:integration).
  // End-to-end tests are Playwright, in e2e/.
  test: {
    projects: [
      {
        extends: true,
        test: {
          name: 'unit',
          environment: 'jsdom',
          include: ['tests/unit/**/*.test.{ts,tsx}'],
          setupFiles: ['tests/setup.ts'],
        },
      },
      {
        extends: true,
        test: {
          name: 'integration',
          environment: 'jsdom',
          include: ['tests/integration/**/*.test.{ts,tsx}'],
          setupFiles: ['tests/setup.ts', 'tests/integration/msw-setup.ts'],
        },
      },
    ],
  },
})
