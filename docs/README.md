# Contract-First Development with the spring-react-openapi-template

## Metadata

| Field          | Value                                                                                 |
| -------------- | ------------------------------------------------------------------------------------- |
| Date           | 2026-11-12                                                                            |
| Purpose        | Entry point to the docs: what contract-first means here and where to read next        |
| Tags           | `Architecture > API Design`, `Modernization > Strategy`, `OpenAPI`, `Templates > Onboarding` |
| Primary Intent | Show a skeptical reader why the API spec comes before the code, then how to use the templates. |

## The key idea

In a large modernization project, the most expensive mistakes are disagreements about interfaces
that nobody notices until integration. Writing the OpenAPI contract first moves those disagreements
to the start of the work, where a change costs one pull request against one file instead of
rework in several teams' code.

This template makes that the default path. `openapi/openapi.yaml` is the only place an endpoint or a
payload is defined. The Spring backend and the React frontend both generate their types from it at
build time, so if either side drifts from the contract, that side stops compiling.

## Who these docs are for

- Engineers and leads who are used to code-first (write the controller, let Swagger describe it later)
  and want to know what they get for the extra step.
- Anyone starting a service from this template who needs the workflow, not the theory.

## Reading order

| Doc | Read it if you want to know | Framework used |
| --- | --------------------------- | -------------- |
| [01 - Why contract-first](01-why-contract-first.md) | why spec-first pays off in modernization, and where it doesn't | Pyramid, 5 Whys, SWOT |
| [02 - How the template works](02-how-the-template-works.md) | what generates what, and how the two services fit together | Problem / Solutions / Next Steps |
| [03 - Day-to-day workflow](03-workflow.md) | how to add or change an endpoint, and who signs off | Steps, RACI |
| [04 - Setup and configuration](04-setup-and-configuration.md) | how to configure Keycloak, PostgreSQL and deployment | Steps, tables |
| [05 - Rolling it out](05-adoption-plan.md) | how to introduce contract-first on a modernization program | GROW, OKR, SMART |

## Quick start

1. Read [01](01-why-contract-first.md) if you need convincing, otherwise skip to step 2.
2. Configure Keycloak and the database with [04](04-setup-and-configuration.md).
3. Follow "Running locally" in the root [README](../README.md).
4. Add your first endpoint by following [03](03-workflow.md).

## Changelog

| Date       | Description                    |
| ---------- | ------------------------------ |
| 2026-11-12 | Initial docs set (index + 01-05) |
| 2026-11-12 | Scoped to this template's stack (Keycloak + PostgreSQL); 04 replaced with setup guide |

## Revision History

| Version | Date       | Description              |
| ------- | ---------- | ------------------------ |
| 1.0     | 2026-11-12 | First version of the docs index |
| 1.1     | 2026-11-12 | Scoped to this template's stack only |
