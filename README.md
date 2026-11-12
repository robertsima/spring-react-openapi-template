# spring-react-openapi-template
## Spec-driven monorepo: one OpenAPI contract → Spring Boot backend + React frontend, Windows SSO, Oracle or SQL Server

> **Branch `feature/windows-sso-oracle-sqlserver`.** This variant targets Windows/Active Directory shops.
> Keycloak/JWT is replaced by Windows SSO (Waffle or Spring Security Kerberos), and Postgres by Oracle
> or SQL Server. `main` has the Keycloak + Postgres variant.

A template for contract-first development. The API lives in a single file, `openapi/openapi.yaml`, and both
sides are generated from it:

* **backend/**: a Spring Boot 4 / Java 25 microservice built from [spring-boot-starter-template](https://github.com/robertsima/spring-boot-starter-template).
  Controllers implement the generated `*Api` interfaces and return the generated DTOs.
* **frontend/**: a React + TypeScript (Vite) app adapted from [react-springboot-appengine-template](https://github.com/sparmboy/react-springboot-appengine-template).
  It calls the backend only through the generated `typescript-fetch` client.

The frontend and backend are **separate deployables**. Each has its own image, its own pod and its own CPU/memory limits.

For security purposes, you should never store credentials inside this project - only as an environment variable or key in some other place.

## Goals
- [x] Single OpenAPI contract shared by backend and frontend
- [x] Windows SSO (SPNEGO/Kerberos): **Waffle** on Windows hosts or **Spring Security Kerberos** in Linux containers
- [x] AD group / account → application role mapping in config (`app.security.role-mappings`)
- [x] Fail-closed security when no auth profile is active, plus a `localdev` fake-SSO profile for development
- [x] **Oracle** or **SQL Server**, chosen by Spring profile, with DB-neutral Liquibase changelogs
- [x] Testcontainers integration tests on either database (`-Dit.db=sqlserver|oracle`)
- [x] Unit / integration test folders for both services, Playwright e2e for the UI
- [x] Prometheus metrics (actuator + nginx exporter), alert rule and Grafana placeholders
- [x] Structured JSON logs in ECS format from the backend, nginx and the browser logger
- [x] STOMP/SockJS WebSockets, Lombok + MapStruct, MUI theme, Storybook
- [x] Podman/kube yaml per database with per-service resource limits and health probes

## How to use
1. Use this repo as a template (or clone / download the zip) and check out this branch.
2. Rename the placeholders: `your_repo_name`, `com.example_project_name`, `MYDOMAIN`, `app.mydomain.com`.
3. Edit `openapi/openapi.yaml`, regenerate, implement, and start developing!

If you use this and end up releasing, all I ask for is a reference to this project somehow.

## Profiles

Activate **one database profile and one auth profile**: `SPRING_PROFILES_ACTIVE=<db>,<auth>`.

| Profile     | Kind | What it does                                                                                     | Runs on            |
| ----------- | ---- | ------------------------------------------------------------------------------------------------ | ------------------ |
| `sqlserver` | DB   | `mssql-jdbc`, `application-sqlserver.yml`                                                        | anywhere           |
| `oracle`    | DB   | `ojdbc11`, `application-oracle.yml`                                                              | anywhere           |
| `waffle`    | auth | Waffle validates Negotiate tokens with Windows SSPI and reads AD groups for roles                | **Windows host only** |
| `kerberos`  | auth | Spring Security Kerberos validates SPNEGO tickets with a keytab; roles map from the principal name | anywhere (Linux image) |
| `localdev`  | auth | every request is a fake user from `application-localdev.yml`. **Development only.**              | anywhere           |
| *(none)*    | auth | fail-closed: all protected endpoints return 401 (what tests use, with `@WithMockUser`)           | -                  |

## Windows SSO

There's no login screen and no token handling in the UI. The first API call gets `401 WWW-Authenticate: Negotiate`,
and the browser answers with the user's Kerberos ticket and retries. The frontend then calls `GET /me` (part of the
contract) to learn the username and roles.

**Browser side:** the site must be trusted for Negotiate. Use the Local Intranet zone, the `AuthServerAllowlist` policy
for Edge/Chrome, or `network.negotiate-auth.trusted-uris` for Firefox.

**Roles:** `app.security.role-mappings` maps identities to roles. Every user also gets `ROLE_USER`.

```yaml
app:
  security:
    role-mappings:
      "[MYDOMAIN\\App-Admins]": ADMIN     # AD group (waffle)
      "[jdoe@MYDOMAIN.COM]": ADMIN       # principal (kerberos, or waffle)
```

* **waffle** gets the user's AD groups from Windows directly. The backend must run on a domain-joined
  **Windows** machine: `java -jar app.jar --spring.profiles.active=sqlserver,waffle`. Point the frontend's
  `BACKEND_URL` at that host.
* **kerberos** runs in the Linux container. Your AD admin registers an SPN for the host name users browse to
  (the frontend), e.g. `setspn -S HTTP/app.mydomain.com svc-app`, and exports a keytab. Mount the keytab and a
  `krb5.conf` at `/etc/krb5` (see `deploy/*.yaml`). A Kerberos ticket has no group list, so roles map from the
  principal name. To use AD groups, add an LDAP lookup in `KerberosSecurityConfig.kerberosUserDetailsService`.
* **Through the proxy:** only `Negotiate` is offered. Kerberos tokens are per request, so they pass through nginx.
  Raw NTLM is tied to a single TCP connection and does not.
* **CSRF stays on:** browsers send Windows credentials automatically, just as they send cookies. The backend issues an
  `XSRF-TOKEN` cookie, and the generated client's middleware sends it back as `X-XSRF-TOKEN`.

## Contract-first workflow

1. Add or change an operation or schema in `openapi/openapi.yaml`.
2. Backend: `cd backend && ./mvnw generate-sources` (runs as part of any build). Implement the new method from the
   generated interface in `com.example_project_name.generated.api` inside a `@RestController` that `implements` it.
   Override the method only, and don't redeclare `@GetMapping` etc.
3. Frontend: `cd frontend && npm run generate:api`, which also runs on `npm run dev`. Use the new method through
   `useAppContext().apis`.

Generated code is never committed: `backend/target/generated-sources/openapi` and `frontend/src/generated`.

> Generator versions are pinned in three places. Keep them in sync: `backend/pom.xml`, `frontend/openapitools.json`
> and the `openapi-generator-cli` image tag in `frontend/Dockerfile`.

## Running locally

Prerequisites: JDK 25, Node 22, and podman (or docker). The frontend generator needs Java.

```bash
# 1. A database. SQL Server shown; Oracle: docker.io/gvenzl/oracle-free:23-slim with APP_USER/APP_USER_PASSWORD
podman run -d --name sqlserver -p 1433:1433 -e ACCEPT_EULA=Y -e 'MSSQL_SA_PASSWORD=SaPassw0rd!Change' \
  mcr.microsoft.com/mssql/server:2022-latest
podman exec sqlserver /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P 'SaPassw0rd!Change' \
  -Q "CREATE DATABASE appdb; CREATE LOGIN appuser WITH PASSWORD='AppPassw0rd!';" \
  && podman exec sqlserver /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P 'SaPassw0rd!Change' -d appdb \
  -Q "CREATE USER appuser FOR LOGIN appuser; ALTER ROLE db_owner ADD MEMBER appuser;"

# 2. Backend on :8080 with fake SSO (VS Code: copy .env.example to .env and use the launch config)
cd backend && SPRING_PROFILES_ACTIVE=sqlserver,localdev \
  SPRING_DATASOURCE_PASSWORD='AppPassw0rd!' ./mvnw spring-boot:run

# 3. Frontend on :3000 (Vite proxies /api and /websockets to :8080)
cd frontend && npm install && npm run dev
```

On a Windows dev machine you can use `sqlserver,waffle` instead of `localdev` to get your real AD identity.

## Tests

| Service  | Kind        | Where                                   | Command                                        |
| -------- | ----------- | --------------------------------------- | ---------------------------------------------- |
| backend  | unit        | `backend/src/test/java/unit`            | `./mvnw test`                                  |
| backend  | integration | `backend/src/test/java/integration`     | `./mvnw verify` / `./mvnw verify -Dit.db=oracle` |
| frontend | unit        | `frontend/tests/unit`                   | `npm run test:unit`                            |
| frontend | integration | `frontend/tests/integration` (MSW)      | `npm run test:integration`                     |
| frontend | e2e         | `frontend/e2e` (Playwright)             | `npm run test:e2e`                             |

Each folder has a README with its conventions. Backend integration tests need a container runtime for Testcontainers.

## Containers / deployment

Both images build with the **repo root** as context, because both need `openapi/`. The pods find each other by name,
so put them on a DNS-enabled network:

```bash
podman network create app-net
podman build -f backend/Dockerfile  -t your_repo_name-backend:latest  .
podman build -f frontend/Dockerfile -t your_repo_name-frontend:latest .
podman play kube --network app-net deploy/podman-kube-sqlserver.yaml   # or deploy/podman-kube-oracle.yaml
```

| Pod                           | Image                         | Port         | Requests / limits (edit to taste) |
| ----------------------------- | ----------------------------- | ------------ | --------------------------------- |
| `sqlserver-pod` / `oracle-pod` | SQL Server 2022 / Oracle 23ai Free | 1433 / 1521 | 500m / 2 CPU, 2Gi / 3-4Gi |
| `spring-app-pod`              | `your_repo_name-backend`      | 8080 (+8081 internal) | 500m / 2 CPU, 512Mi / 1Gi |
| `frontend-pod`                | `your_repo_name-frontend` + nginx exporter | 3000 | 50m / 0.5 CPU, 32Mi / 128Mi |

* The containerised backend uses `kerberos`, since Waffle can't run in Linux. It needs `/opt/your_repo_name/krb5`
  on the host with `app.keytab` and `krb5.conf`.
* The SQL Server pod includes a small init sidecar that creates `appdb` and the `appuser` login. The Oracle image
  creates `appuser` in `FREEPDB1` itself.
* The backend JVM sizes its heap from the container memory limit (`-XX:MaxRAMPercentage=75`).

## Monitoring and logging

* **Metrics:** the backend serves `/actuator/prometheus` and health probes on the management port **8081**, which is
  not routed through nginx. nginx exposes `stub_status` on an internal port, and an `nginx-prometheus-exporter`
  sidecar publishes it on 9113. Scrape config, alert rules and Grafana provisioning are in `monitoring/` (see its README).
* **Logs:** everything writes JSON lines with **ECS** field names to stdout.
  * The backend uses Spring Boot structured logging (`logging.structured.format.console: ecs`).
  * nginx uses an `ecs_json` access log format.
  * The browser uses `src/utils/logger.ts`.
  * nginx forwards `X-Request-ID`, so frontend and backend lines for one request can be correlated.

## Project Structure Breakdown

```text
spring-react-openapi-template/
  openapi/openapi.yaml      # THE contract (incl. GET /me)

  backend/
    pom.xml                 # Boot 4, Waffle, Spring Security Kerberos, ojdbc11, mssql-jdbc, Liquibase,
                            # Testcontainers (mssqlserver, oracle-free), actuator + Prometheus, Lombok, MapStruct
    src/main/java/com/example_project_name/
      security/             # SecurityRules (shared), Waffle/Kerberos/LocalDev/NoSso configs, RoleMapper
      controller/           # SessionController (/me), UserController - implement generated *Api
      mapper/ model/ repository/ service/ websocket/ app/
    src/main/resources/
      application.yml       # common: actuator, ECS logging, role mappings
      application-{sqlserver,oracle,waffle,kerberos,localdev}.yml
      db/changelog/         # Liquibase XML, DB-neutral
    src/test/java/{unit,integration}/

  frontend/
    src/services/Authentication/  # AuthenticationProvider: GET /me via Windows SSO
    src/services/apiConfig.ts     # generated client + CSRF middleware
    src/utils/logger.ts           # ECS JSON browser logger
    tests/{unit,integration}/     # Vitest (+ MSW)
    e2e/                          # Playwright
    nginx/default.conf.template   # proxy, Negotiate pass-through, ECS access log, stub_status

  deploy/podman-kube-{sqlserver,oracle}.yaml
  monitoring/               # prometheus.yml, alert rules, Grafana provisioning (placeholders)
  .env.example
```
