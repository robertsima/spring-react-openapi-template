# Frontend tests

| Folder          | Tool                          | Scope                                                        | Command                    |
| --------------- | ----------------------------- | ------------------------------------------------------------ | -------------------------- |
| `unit/`         | Vitest + Testing Library      | pure functions and components, no network                    | `npm run test:unit`        |
| `integration/`  | Vitest + Testing Library + MSW | providers + generated API client against a mocked backend    | `npm run test:integration` |
| `../e2e/`       | Playwright                    | full stack in a real browser                                 | `npm run test:e2e`         |

Integration tests import types from `src/generated/api`, so run `npm run generate:api` first (`npm run dev` does).
