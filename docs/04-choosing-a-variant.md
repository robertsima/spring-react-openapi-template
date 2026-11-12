# 04 - Choosing a Variant

## Metadata

| Field          | Value                                                                                  |
| -------------- | -------------------------------------------------------------------------------------- |
| Date           | 2026-11-12                                                                             |
| Purpose        | Help a team pick the branch and runtime profiles that match their environment           |
| Tags           | `Templates > Variants`, `Security > Authentication`, `Data > Databases`, `Operations`     |
| Primary Intent | Match the template variant to the identity provider and database you already run.      |

## Key point

Both variants use the same contract-first pipeline. Pick by **identity provider** first, then by
**database**. Everything else follows.

## Decision table

| Your environment | Use | Profiles |
| ---------------- | --- | -------- |
| Keycloak or another OIDC provider, PostgreSQL | `main` | (none needed) |
| Active Directory, users on domain-joined Windows machines, backend in Linux containers | `feature/windows-sso-oracle-sqlserver` | `sqlserver,kerberos` or `oracle,kerberos` |
| Active Directory, backend on a Windows server | `feature/windows-sso-oracle-sqlserver` | `sqlserver,waffle` or `oracle,waffle` |
| Local development without AD | `feature/windows-sso-oracle-sqlserver` | `<db>,localdev` |
| Tests (any) | either | no auth profile; tests use `@WithMockUser` |

## Variant: `main`

- **Auth:** the React app logs in with Keycloak (Authorization Code + PKCE) and sends a bearer token. The
  backend is a stateless JWT resource server that trusts one or more issuers and maps Keycloak realm roles
  to `ROLE_*`.
- **Database:** PostgreSQL with Liquibase and Testcontainers.
- **Local Keycloak:** `keycloak/your-realm.json` imports a realm with test users.

## Variant: `feature/windows-sso-oracle-sqlserver`

### Authentication options

| | `waffle` | `kerberos` | `localdev` |
| --- | --- | --- | --- |
| Validates | Negotiate token via Windows SSPI | SPNEGO ticket via keytab | nothing (fake user) |
| Runs on | Windows host only | anywhere | anywhere |
| Roles from | AD groups + account name | principal name (add LDAP for groups) | configured groups |
| Needs from AD admins | domain-joined server | service account, SPN, keytab | nothing |
| Use for | Windows server deployments | containers / Kubernetes | development only |

With no auth profile active, every protected endpoint returns 401. A misconfigured deployment fails
closed instead of open.

Roles are mapped in one place, `app.security.role-mappings`, for every profile. The UI learns them from
`GET /me`, which is part of the contract.

### Database options

| | `sqlserver` | `oracle` |
| --- | --- | --- |
| Driver | `mssql-jdbc` | `ojdbc11` |
| Local container | SQL Server 2022 (Developer edition, EULA acceptance required) | Oracle 23ai Free (`gvenzl/oracle-free`) |
| Podman stack | `deploy/podman-kube-sqlserver.yaml` | `deploy/podman-kube-oracle.yaml` |
| Integration tests | `./mvnw verify` (default) | `./mvnw verify -Dit.db=oracle` |

Liquibase changelogs are written in Liquibase XML, not raw SQL, so one changelog works on both
databases. Use `dbms="oracle"` or `dbms="mssql"` on a changeset when you need vendor-specific SQL.

### Extras on this branch

- **Tests:** unit and integration folders for both services. The frontend uses Vitest projects and MSW.
  Playwright e2e tests are in `frontend/e2e`.
- **Monitoring:** `/actuator/prometheus` on management port 8081 and an nginx exporter sidecar.
  Prometheus config, alert rules and Grafana provisioning are in `monitoring/`.
- **Logging:** ECS-format JSON from the backend, nginx and the browser logger, with `X-Request-ID` for
  correlation.

## Switching later

Because the API is defined in the contract, switching identity provider or database touches the
backend's security config, the frontend's authentication provider, config files and deploy yaml.
Controllers, generated code and the UI's data calls stay the same. The `/me` endpoint is the only
contract difference between the branches.

## Changelog

| Date       | Description     |
| ---------- | --------------- |
| 2026-11-12 | Initial version |

## Revision History

| Version | Date       | Description     |
| ------- | ---------- | --------------- |
| 1.0     | 2026-11-12 | Initial version |
