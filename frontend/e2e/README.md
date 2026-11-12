# Frontend end-to-end tests (Playwright)

Browser tests against a running stack: frontend, backend and database.

```bash
npx playwright install            # once: downloads browsers
npm run test:e2e                  # BASE_URL=http://localhost:3000 by default
```

* `specs/`: test files (`*.spec.ts`). Start with user journeys, not component details.
* Auth: run the backend with `SPRING_PROFILES_ACTIVE=<db>,localdev` so tests don't need a domain account.
  To test real Windows SSO, run on a domain-joined agent with the site in the browser's auth-server allowlist
  (`--auth-server-allowlist` for Chromium).
* Data: the backend's Liquibase test changelog seeds users. Add more seed data there rather than through the UI.
