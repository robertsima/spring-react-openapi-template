import {defineConfig, devices} from '@playwright/test';

/**
 * End-to-end tests against a running stack (placeholder).
 *   BASE_URL defaults to the Vite dev server; point it at the podman stack (http://localhost:3000) or a test env.
 *   The backend should run with the "localdev" profile so no real Windows SSO is needed - or run these from a
 *   domain-joined agent with the site in the browser's auth-server allowlist.
 */
export default defineConfig({
    testDir: './specs',
    fullyParallel: true,
    retries: process.env.CI ? 2 : 0,
    reporter: [['list'], ['html', {open: 'never'}]],
    use: {
        baseURL: process.env.BASE_URL ?? 'http://localhost:3000',
        trace: 'on-first-retry',
    },
    projects: [
        {name: 'chromium', use: {...devices['Desktop Chrome']}},
        {name: 'msedge', use: {...devices['Desktop Edge'], channel: 'msedge'}},
    ],
});
