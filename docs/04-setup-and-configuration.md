# 04 - Setup and Configuration

## Metadata

| Field          | Value                                                                                   |
| -------------- | --------------------------------------------------------------------------------------- |
| Date           | 2026-11-12                                                                              |
| Purpose        | Configure authentication, the database and deployment for this template                  |
| Tags           | `Templates > Setup`, `Security > Keycloak`, `Data > PostgreSQL`, `Operations > Deployment` |
| Primary Intent | Get from a fresh clone to a running stack with your own Keycloak realm and database.     |

## Key point

The contract-first pipeline needs no configuration. What you configure is around it: who can log in
(Keycloak), where data lives (PostgreSQL), and where the two services run.

## Steps

1. **Rename the placeholders** across the repo: `your_repo_name`, `com.example_project_name`,
   `your-realm`, `<your-keycloak-host>`.
2. **Point the backend at your issuer(s)** (see [Authentication](#authentication)).
3. **Create the frontend's Keycloak client** and set the frontend's runtime config.
4. **Provide database connection settings** through environment variables.
5. **Build both images and deploy** with `deploy/podman-kube.yaml` or your own manifests.

## Authentication

### How it works

| Side | What happens | Where |
| ---- | ------------ | ----- |
| Browser | Logs in with Keycloak (Authorization Code + PKCE, public client, silent renew) | `frontend/src/main.tsx`, `react-oidc-context` |
| Browser | Sends `Authorization: Bearer <access token>` on every API call and on the STOMP `CONNECT` frame | `services/AppContext.tsx`, `App.tsx` |
| Backend | Validates the JWT against a list of trusted issuers, fetching each issuer's keys on first use | `config/SpringSecurityConfig.java` |
| Backend | Maps Keycloak realm roles to `ROLE_<UPPERCASE>` so `hasRole("ADMIN")` works | `config/KeycloakRoleConverter.java` |
| Browser | Applies the same role mapping for route guards (UI convenience only) | `utils/auth/auth.ts` |

### Backend settings

Trusted issuers are configured in `backend/src/main/resources/application.yml`. Either list them:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uris:
            - https://keycloak.example.com/realms/your-realm
```

or set a single Keycloak realm, and the issuer URL is built from it:

```yaml
keycloak:
  auth-server-url: https://keycloak.example.com
  realm: your-realm
```

Both forms can be combined. Tokens from any other issuer are rejected.

### Frontend settings

| Setting | Dev (`frontend/public/config.json`) | Container (env var) |
| ------- | ----------------------------------- | ------------------- |
| Keycloak realm URL, as the browser sees it | `oidc.authority` | `OIDC_AUTHORITY` |
| Public client ID | `oidc.clientId` | `OIDC_CLIENT_ID` |

The Keycloak client needs: public client, standard flow on, PKCE `S256`, and redirect URIs and web origins
for the frontend URL. `keycloak/your-realm.json` is a working example you can import.

### Local Keycloak

```bash
podman run -d --name keycloak -p 8180:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -v ./keycloak:/opt/keycloak/data/import:Z \
  quay.io/keycloak/keycloak:26.0 start-dev --import-realm
```

This imports realm `your-realm` with users `testuser` / `test` (role `user`) and `admin` / `admin`
(roles `user`, `admin`).

> **Issuer gotcha in containers.** The `iss` claim in a token must exactly match a trusted issuer, and the
> backend must be able to reach that URL to fetch keys. Inside a pod, `localhost` is the pod itself. Use a
> Keycloak host name that both the browser and the backend pod can resolve.

## Database

| Item | Value |
| ---- | ----- |
| Engine | PostgreSQL |
| Schema management | Liquibase, `db/changelog/db.changelog.xml` (prod) and `db.changelog-test.xml` (adds seed data for tests) |
| Connection | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` environment variables |
| Integration tests | Testcontainers starts a Postgres container per run (`PostgresBaseIT`) |

Never commit real credentials. Pass them as environment variables or from your secret store.

## Running and deploying

| Task | Command |
| ---- | ------- |
| Backend locally | `cd backend && ./mvnw spring-boot:run` (with the datasource and Keycloak env vars set) |
| Frontend locally | `cd frontend && npm install && npm run dev` (port 3000, proxies `/api` and `/websockets` to 8080) |
| Unit tests | `cd backend && ./mvnw test` |
| Integration tests | `cd backend && ./mvnw verify` (needs Docker or Podman for Testcontainers) |
| Build images | `podman build -f backend/Dockerfile -t your_repo_name-backend:latest .` and the same with `frontend/Dockerfile` |
| Run the stack | `podman play kube deploy/podman-kube.yaml` |

Both images build from the repo root because both need `openapi/`.

### Resource sizing

`deploy/podman-kube.yaml` gives each service its own pod and limits, so they scale separately:

| Pod | Requests | Limits |
| --- | -------- | ------ |
| `postgres-pod` | 250m CPU, 256Mi | 1 CPU, 512Mi |
| `spring-app-pod` | 500m CPU, 512Mi | 2 CPU, 1Gi |
| `frontend-pod` | 50m CPU, 32Mi | 0.5 CPU, 128Mi |

The backend JVM sizes its heap to 75% of the container memory limit.

## Checklist

- [ ] Placeholders renamed
- [ ] Issuer URLs set and reachable from the backend
- [ ] Keycloak client created with the frontend's redirect URIs
- [ ] Datasource env vars set, no credentials in the repo
- [ ] Both images build from the repo root
- [ ] Resource limits adjusted to your load

## Changelog

| Date       | Description                                                       |
| ---------- | ----------------------------------------------------------------- |
| 2026-11-12 | Replaced "Choosing a variant" with setup for this template's stack |

## Revision History

| Version | Date       | Description                                   |
| ------- | ---------- | --------------------------------------------- |
| 1.0     | 2026-11-12 | Initial version ("Choosing a variant")        |
| 1.1     | 2026-11-12 | Rewritten as setup and configuration for `main` only |
