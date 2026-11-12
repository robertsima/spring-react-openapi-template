# spring-react-openapi-template
## Spec-driven monorepo: one OpenAPI contract → Spring Boot backend + React frontend, Liquibase-managed PostgreSQL, Keycloak JWT auth

A template for contract-first development. The API lives in a single file, `openapi/openapi.yaml`, and both
sides are generated from it:

* **backend/**: a Spring Boot 4 / Java 25 microservice built from [spring-boot-starter-template](https://github.com/robertsima/spring-boot-starter-template).
  Controllers implement the generated `*Api` interfaces and return the generated DTOs.
* **frontend/**: a React + TypeScript (Vite) app adapted from [react-springboot-appengine-template](https://github.com/sparmboy/react-springboot-appengine-template).
  It calls the backend only through the generated `typescript-fetch` client.

Change the contract, regenerate, and the compiler on each side shows you what no longer matches.

The frontend and backend are **separate deployables**. Each has its own image, its own pod and its own CPU/memory limits, so you can scale and size them independently.

For security purposes, you should never store credentials inside this project - only as an environment variable or key in some other place.

## Goals
- [x] Single OpenAPI contract shared by backend and frontend
- [x] Backend: generated Spring interfaces/models (openapi-generator `spring`, interface-only)
- [x] Frontend: generated TypeScript client/models (openapi-generator `typescript-fetch`), same generator version
- [x] Stateless Spring Security JWT resource server, single or multiple issuers, Keycloak realm roles → `ROLE_*`
- [x] React OIDC login against Keycloak (Authorization Code + PKCE, silent renew)
- [x] PostgreSQL + Liquibase (prod and test changelogs), Testcontainers integration tests
- [x] STOMP/SockJS WebSockets with bearer-token auth on CONNECT
- [x] Lombok + MapStruct (entity → generated DTO mapping)
- [x] Material UI theme, loading screen, role-protected routes, Storybook
- [x] Separate Dockerfiles (Temurin JRE / unprivileged nginx) and a podman/kube yaml with per-service resources

## How to use
1. Use this repo as a template (or clone / download the zip).
2. Rename the placeholders: `your_repo_name`, `com.example_project_name`, `your-realm`, `<your-keycloak-host>`.
3. Edit `openapi/openapi.yaml`, regenerate, implement, and start developing!

If you use this and end up releasing, all I ask for is a reference to this project somehow.

## Contract-first workflow

1. Add or change an operation or schema in `openapi/openapi.yaml`.
2. Backend: `cd backend && ./mvnw generate-sources` (runs as part of any build). Implement the new method from the
   generated interface in `com.example_project_name.generated.api` inside a `@RestController` that `implements` it.
   Override the method only. Don't redeclare `@GetMapping` etc., since the mapping comes from the interface.
3. Frontend: `cd frontend && npm run generate:api`, which also runs on `npm run dev`. Use the new method from
   `src/generated/api` through `useAppContext().apis`.

Generated code is never committed and should not be edited: `backend/target/generated-sources/openapi` and
`frontend/src/generated`. Schemas that aren't used by any path, such as `WebSocketEvent`, are still generated on
both sides, which is a handy way to share message types.

> Generator versions are pinned in three places. Keep them in sync: `backend/pom.xml` (`openapi-generator-maven-plugin`),
> `frontend/openapitools.json` and the `openapi-generator-cli` image tag in `frontend/Dockerfile`.

## Running locally

Prerequisites: JDK 25, Node 22, and podman (or docker). The frontend generator needs Java, which the backend already requires.

```bash
# 1. Keycloak with a ready-made realm: client your_repo_name-frontend, users testuser/test and admin/admin
podman run -d --name keycloak -p 8180:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -v ./keycloak:/opt/keycloak/data/import:Z \
  quay.io/keycloak/keycloak:26.0 start-dev --import-realm

# 2. Postgres
podman run -d --name postgres -p 5432:5432 \
  -e POSTGRES_DB=appdb -e POSTGRES_USER=appuser -e POSTGRES_PASSWORD=apppassword postgres:17

# 3. Backend on :8080. VS Code: copy .env.example to .env and use the launch config, or:
cd backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/appdb SPRING_DATASOURCE_USERNAME=appuser \
SPRING_DATASOURCE_PASSWORD=apppassword KEYCLOAK_AUTH_SERVER_URL=http://localhost:8180 KEYCLOAK_REALM=your-realm \
./mvnw spring-boot:run

# 4. Frontend on :3000 (Vite proxies /api and /websockets to :8080)
cd frontend && npm install && npm run dev
```

Open http://localhost:3000, sign in as `admin` / `admin`, search for a user, and open the admin page to watch the
WebSocket-driven switch. Swagger UI is at http://localhost:8080/swagger-ui.html.

Tests (backend): `./mvnw test` runs unit tests and `./mvnw verify` runs the integration tests; these need a container runtime for Testcontainers.
Storybook (frontend): `npm run storybook`.

## Containers / deployment

Both images build with the **repo root** as context, because both need `openapi/`:

```bash
podman build -f backend/Dockerfile  -t your_repo_name-backend:latest  .
podman build -f frontend/Dockerfile -t your_repo_name-frontend:latest .
podman play kube deploy/podman-kube.yaml
```

| Pod              | Image                         | Port | Requests / limits (edit to taste) |
| ---------------- | ----------------------------- | ---- | --------------------------------- |
| `postgres-pod`   | `postgres:17`                 | -    | 250m / 1 CPU, 256Mi / 512Mi       |
| `spring-app-pod` | `your_repo_name-backend`      | 8080 | 500m / 2 CPU, 512Mi / 1Gi         |
| `frontend-pod`   | `your_repo_name-frontend`     | 3000 | 50m / 0.5 CPU, 32Mi / 128Mi       |

* The **frontend** container is nginx. It serves the SPA, reverse-proxies `/api/*` (prefix stripped) and `/websockets` to
  `BACKEND_URL`, and renders `/config.json` from `OIDC_AUTHORITY` / `OIDC_CLIENT_ID` at start-up. The same image
  can be promoted across environments, and the browser only ever talks to one origin, so no CORS is needed.
* The **backend** JVM sizes its heap from the container memory limit (`-XX:MaxRAMPercentage=75`).
* **Issuer gotcha:** the backend fetches the issuer's metadata, and the `iss` claim in tokens must match exactly. Inside a pod,
  `localhost` is the pod itself, so point both `OIDC_AUTHORITY` (browser) and the backend issuer at a Keycloak hostname
  that both can resolve.

## Project Structure Breakdown

```text
spring-react-openapi-template/
  openapi/
    openapi.yaml            # THE contract. Source of truth for both services.

  backend/                  # Spring Boot microservice (Maven, Java 25)
    pom.xml                 # Boot 4, Postgres, Liquibase, Testcontainers, OAuth2 resource server, OpenFeign,
                            # SpringDoc, WebSocket, Lombok, MapStruct, openapi-generator (spec: ../openapi)
    Dockerfile              # multi-stage Maven -> Temurin JRE
    src/main/java/com/example_project_name/
      app/                  # MyServiceApplication
      config/               # SpringSecurityConfig (multi-issuer JwtDecoder), KeycloakRoleConverter
      controller/           # @RestControllers implementing generated *Api interfaces
      mapper/               # MapStruct mappers: entities -> generated DTOs
      model/                # JPA entities
      repository/           # Spring Data repositories
      service/              # service interfaces + implementations
      websocket/            # STOMP config (JWT on CONNECT) + sample broadcaster
    src/main/resources/
      application.yml
      db/changelog/         # Liquibase
    src/test/java/
      integration/          # Testcontainers Postgres ITs
      unit/                 # Mockito unit tests

  frontend/                 # React + TypeScript + Vite
    package.json            # MUI, react-router, react-oidc-context, react-stomp-hooks, Storybook, openapi-generator-cli
    openapitools.json       # pins the generator version
    Dockerfile              # generator image -> node build -> unprivileged nginx
    nginx/default.conf.template
    public/config.json      # dev runtime config (nginx renders it in containers)
    src/
      config.ts             # runtime config loader
      services/             # generated-client wiring (apiConfig.ts) + AppContext
      components/routes/    # RequireAuth (OIDC gate), ProtectedRoute (role guard)
      screens/              # Home, Admin (WebSocket demo), Loading, Unauthorised
      themes/               # MUI theme
      generated/api/        # (generated, git-ignored)

  keycloak/your-realm.json  # dev realm import: roles user/admin, SPA client, test users
  deploy/podman-kube.yaml   # postgres, backend, frontend pods with resource limits
  .env.example              # local backend env for the VS Code launch config
```

### Notes

* **Roles**: Keycloak realm roles (`realm_access.roles`) become `ROLE_<UPPERCASE>` on both sides
  (`KeycloakRoleConverter.java` and `frontend/src/utils/auth/auth.ts`). Route guards in the UI are a convenience only. The
  backend enforces access on every request.
* **WebSockets**: the SockJS handshake (`/websockets/**`) is open. The STOMP `CONNECT` frame must carry
  `Authorization: Bearer <token>`, which is validated with the same issuer list as the REST API.
* **DTOs** come from the contract, so entities stay decoupled from the API and MapStruct does the mapping.
* `MessageBroadcastSchedule`, `TestController`, the `Users` entity and the sample screens are examples. Delete them
  once you have real code.
