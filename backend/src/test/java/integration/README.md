# Backend integration tests

Full Spring context against a real database in Testcontainers (needs Docker or Podman).
Run with `./mvnw verify` (failsafe, `*IT` classes).

* Extend `DatabaseBaseIT`. It starts one shared container for the whole run and applies the Liquibase
  test changelog (`src/test/resources/db/changelog/db.changelog-test.xml`, schema + seed data).
* Choose the database with `-Dit.db=sqlserver` (default) or `-Dit.db=oracle`, and run both in CI.
* No SSO profile is active in tests, so security is fail-closed. Use `@WithMockUser(roles = ...)` to authenticate.
  Unsafe methods (POST/PUT/DELETE) also need `.with(csrf())`.
