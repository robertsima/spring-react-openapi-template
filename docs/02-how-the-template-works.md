# 02 - How the Template Works

## Metadata

| Field          | Value                                                                                 |
| -------------- | ------------------------------------------------------------------------------------- |
| Date           | 2026-11-12                                                                            |
| Purpose        | Explain what the template generates from the contract and how the two services connect |
| Tags           | `Architecture > Code Generation`, `OpenAPI > Tooling`, `Spring Boot`, `React`           |
| Primary Intent | Give a developer a working model of the build so they know where to make each change.  |

## Problem

Two services written in two languages need to agree on every path, parameter and payload, and keep
agreeing as both change. Hand-maintained DTOs on each side drift apart quietly.

## Solution

One contract file, two generators, and a build that refuses to compile when code and contract disagree.

```text
                      openapi/openapi.yaml
                     /                    \
   openapi-generator "spring"        openapi-generator "typescript-fetch"
   (Maven, generate-sources)         (npm run generate:api, or Docker stage)
            |                                   |
   generated.api.*Api interfaces       src/generated/api: UsersApi, SessionApi...
   generated.model.* DTOs              models + FromJSON/ToJSON helpers
            |                                   |
   @RestController implements *Api     useAppContext().apis.usersApi.getUsersByName()
            |                                   |
        backend image  <---- nginx /api ----  frontend image
```

### Repository layout

| Path | Owns |
| ---- | ---- |
| `openapi/openapi.yaml` | Every endpoint, parameter, payload and shared message type. The only hand-edited API definition. |
| `backend/` | Spring Boot service. Implements the generated interfaces. Owns persistence, security, business logic. |
| `frontend/` | React + Vite app. Calls the backend only through the generated client. |
| `deploy/` | Podman / kube yaml, one pod per service with its own resource limits. |

### Backend generation

- `backend/pom.xml` runs `openapi-generator-maven-plugin` with generator `spring` and `interfaceOnly`.
- Output goes to `target/generated-sources/openapi` in the packages `com.example_project_name.generated.api`
  and `...generated.model`. It's never committed.
- A controller implements the interface and overrides only the method. The path, HTTP method, parameter
  binding and bean validation (`@Size`, `@NotNull` from the spec) come from the interface.

```java
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {
    private final UserService userService;

    @Override
    public ResponseEntity<List<UserDTO>> getUsersByName(String name) {
        return ResponseEntity.ok(userService.getUsersByName(name));
    }
}
```

- Entities stay separate from DTOs. MapStruct (`mapper/UserMapper`) converts entities to the generated DTOs,
  so a database column change doesn't leak into the API, and the reverse.

### Frontend generation

- `npm run generate:api` runs `@openapitools/openapi-generator-cli` with generator `typescript-fetch`
  into `src/generated/api` (git-ignored). `npm run dev` runs it automatically.
- The Docker build runs the same generator in a separate stage using the official generator image, so the
  Node stage doesn't need a JDK.
- `src/services/apiConfig.ts` creates the clients once, with the base path from runtime config.

```ts
const {apis} = useAppContext();
const users = await apis.usersApi.getUsersByName({name}); // typed from the spec
```

### Shared message types

Schemas that no path references are still generated on both sides. The template uses that for
WebSocket payloads: `WebSocketEvent` is in the contract, so the backend publishes the generated Java class
and the frontend parses it with the generated `WebSocketEventFromJSON`.

### Runtime topology

- The browser only talks to the frontend's nginx. nginx serves the SPA and proxies `/api/*` (prefix
  stripped) and `/websockets` to the backend, so there's no CORS configuration.
- The frontend reads `/config.json` at startup. nginx renders it from environment variables, so one image
  works in every environment.
- Each service has its own Dockerfile, image and pod, so CPU and memory can be sized separately.
  The JVM sizes its heap from the container memory limit.

### Version pinning

The generator version must match in three places, or the two sides may generate slightly different code:

| Where | Setting |
| ----- | ------- |
| `backend/pom.xml` | `openapi-generator-maven-plugin` version |
| `frontend/openapitools.json` | `generator-cli.version` |
| `frontend/Dockerfile` | `openapitools/openapi-generator-cli` image tag |

## What differs between variants

The generation pipeline is identical on both branches. Security, the database and some extras differ.
See [04 - Choosing a variant](04-choosing-a-variant.md).

| Concern | `main` | `feature/windows-sso-oracle-sqlserver` |
| ------- | ------ | -------------------------------------- |
| Auth | Keycloak OIDC in the UI, JWT resource server in the backend | Windows SSO (Waffle or Kerberos), `GET /me` in the contract |
| Database | PostgreSQL | Oracle or SQL Server by profile |
| Tests | backend unit + integration | backend and frontend unit + integration, Playwright e2e |
| Ops | - | Prometheus metrics, ECS JSON logs |

## Next Steps

- [ ] Make your first change: [03 - Day-to-day workflow](03-workflow.md)
- [ ] Rename the placeholders (`your_repo_name`, `com.example_project_name`) before your first commit

## Changelog

| Date       | Description     |
| ---------- | --------------- |
| 2026-11-12 | Initial version |

## Revision History

| Version | Date       | Description     |
| ------- | ---------- | --------------- |
| 1.0     | 2026-11-12 | Initial version |
