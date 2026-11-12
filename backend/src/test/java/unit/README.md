# Backend unit tests

Fast tests with no Spring context and no database: plain JUnit 5 + Mockito + AssertJ.
Run with `./mvnw test` (surefire, `*Test` classes).

Mirror the main package layout under `unit/com/example_project_name/...`:

| Folder        | What goes here                                                      |
| ------------- | ------------------------------------------------------------------- |
| `service/`    | business logic with mocked repositories (see `UserServiceImplTest`) |
| `mapper/`     | MapStruct mappers, entity <-> generated DTO                         |
| `security/`   | role mapping and other security helpers                             |
| `controller/` | slice tests with `@WebMvcTest` if you need them (no database)       |
