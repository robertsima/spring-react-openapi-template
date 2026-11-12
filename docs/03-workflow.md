# 03 - Day-to-Day Workflow

## Metadata

| Field          | Value                                                                              |
| -------------- | ---------------------------------------------------------------------------------- |
| Date           | 2026-11-12                                                                         |
| Purpose        | Step-by-step guide for adding or changing an endpoint, and who is involved          |
| Tags           | `Process > API Change Management`, `OpenAPI > Contract-First`, `Team > Roles`        |
| Primary Intent | Make the contract-first loop routine: change the spec, regenerate, implement, test. |

## Adding an endpoint

The example adds `GET /users/{id}`.

1. **Write the contract change.** Edit `openapi/openapi.yaml`. Add the path, an `operationId`
   (this becomes the method name on both sides), a tag (this decides which `*Api` interface it lands in),
   parameters and responses. Reuse schemas from `components/schemas`.

   ```yaml
   /users/{id}:
     get:
       tags: [Users]
       operationId: getUserById
       parameters:
         - name: id
           in: path
           required: true
           schema: {type: integer, format: int64}
       responses:
         "200":
           description: The user
           content:
             application/json:
               schema: {$ref: "#/components/schemas/UserDTO"}
         "404":
           description: Not found
   ```

2. **Open a PR with only the contract change**, or mark it clearly in a larger PR. Consumers review the
   shape here. This is the cheapest point to change it.

3. **Regenerate the backend.** Run `cd backend && ./mvnw generate-sources`. `UsersApi` now has
   `getUserById(Long id)` with a default implementation that returns 501.

4. **Implement it.** Override the method in `UserController`. Add service and repository code as needed,
   and map entities to the generated DTO with MapStruct. Don't add `@GetMapping`, because the interface
   already has the mapping.

5. **Regenerate the frontend.** Run `cd frontend && npm run generate:api`. `usersApi.getUserById({id})`
   now exists, fully typed.

6. **Use it in the UI** through `useAppContext().apis.usersApi`.

7. **Test it.** Add a backend unit test for the service (`src/test/java/unit`, run by `./mvnw test`) and an
   integration test for the endpoint (`*IT` extending `PostgresBaseIT`, run by `./mvnw verify`).
   Authenticate in tests with `@WithMockUser(roles = ...)`.

8. **Run the full build** (`./mvnw verify`, `npm run build`). Any place still using the old shape fails to
   compile.

## Changing an existing endpoint

| Change | Breaking? | What to do |
| ------ | --------- | ---------- |
| Add an optional field to a response | No | Change the spec, regenerate, done. |
| Add an optional query parameter | No | Same. |
| Add a required request field | Yes | Coordinate with consumers, or add it as optional first. |
| Rename or remove a field | Yes | Add the new field, migrate consumers, then remove the old one in a later release. |
| Change a type (`string` to `integer`) | Yes | Treat as rename: new field, migrate, remove. |

The compiler finds every affected call site on both sides after regeneration. Use that list to size
the change before you merge it.

## Rules that keep the contract honest

- Never edit generated code. It's git-ignored and overwritten on every build.
- Never add an endpoint with only `@GetMapping` on a plain controller. If it isn't in the spec,
  it doesn't exist for consumers. (`TestController` in the template is a sample, so delete it.)
- Keep generator versions in sync (see [02](02-how-the-template-works.md#version-pinning)).
- Write the spec one slice ahead of implementation, not the whole system upfront.

## Who does what (RACI)

| Activity | Responsible | Accountable | Consulted | Informed |
| -------- | ----------- | ----------- | --------- | -------- |
| Propose a contract change | Developer on the providing team | API owner for the service | Consuming teams | QA |
| Review and merge the contract | API owner | Tech lead / architect | Security (auth or data exposure changes) | All consumers |
| Implement the backend | Providing team | Providing team lead | - | Consumers |
| Implement the frontend / consumers | Consuming team | Consuming team lead | Providing team | - |
| Breaking-change rollout | API owner | Program architect | All consumers, release management | Operations |
| Keep generator versions aligned | Platform / template maintainer | Tech lead | - | All teams |

> Name an API owner for each service before the first contract PR. Without one, reviews get skipped and
> the contract stops being the source of truth.

## Checklist for a contract PR

- [ ] `operationId` is unique and reads well as a method name on both sides
- [ ] Every response code the client must handle is listed
- [ ] Required fields are really required
- [ ] Shared shapes are `$ref`s to `components/schemas`, not copies
- [ ] Breaking changes are called out in the PR description with a migration plan
- [ ] Backend and frontend still build after regeneration

## Changelog

| Date       | Description     |
| ---------- | --------------- |
| 2026-11-12 | Initial version |
| 2026-11-12 | Testing step matches this template's test setup |

## Revision History

| Version | Date       | Description     |
| ------- | ---------- | --------------- |
| 1.0     | 2026-11-12 | Initial version |
| 1.1     | 2026-11-12 | Testing step updated |
