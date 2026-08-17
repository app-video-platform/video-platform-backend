# AGENTS.md

Guidance for Codex sessions maintaining the Video Platform backend.

## Required Context

Before inspecting or modifying this repository, read `PROJECT_CONTEXT.md`
completely.

Treat `AGENTS.md` as the operating instructions and `PROJECT_CONTEXT.md` as the
current architectural and product context. If either conflicts with the current
implementation, verify the implementation and update the stale context.

Generated from repository state:

- Branch: `main`
- Commit: `7ee603c`
- Last reviewed: 2026-08-17

## Repository Purpose

This repository contains the Spring Boot backend for the Video Platform App.
It owns persisted business rules, authentication and authorization, API
contracts, database migrations, file access, and external service integrations.

Related repositories:

- `../video-platform-ui`: React and TypeScript frontend.
- `../video-platform-UI-docs`: Docusaurus product and developer documentation.

Use the current backend implementation as the source of truth for server-side
behavior. Verify cross-application behavior against the frontend before making
claims about complete user flows.

## Project Shape

The application uses Java 17, Spring Boot, Spring MVC, Spring Security, Spring
Data JPA, PostgreSQL, Liquibase, Springdoc OpenAPI, JWT authentication,
DigitalOcean Spaces through the AWS S3 SDK, SendGrid, and external calendar
provider clients.

Primary packages under
`src/main/java/com/myproject/video/video_platform`:

- `controller`: HTTP endpoints grouped by authentication, users, products,
  authoring, entitlements, admin, files, calendars, and quizzes.
- `controller/docs`: Springdoc operation descriptions separated from controller
  implementations.
- `service`: business logic, authorization, integrations, and orchestration.
- `entity`: JPA persistence models.
- `repository`: Spring Data repositories.
- `dto`: public request and response contracts.
- `common/converter`: entity-to-DTO and DTO-to-entity mapping.
- `common/enums`: persisted and API-visible domain values.
- `configs`: security, OpenAPI, S3, and application configuration.
- `exception`: domain exceptions and the global HTTP exception handler.

Database migrations live under `src/main/resources/db/changelog`. Tests mirror
the main package structure under `src/test/java`.

## Source-of-Truth Rules

- Treat controllers, DTOs, services, entities, security configuration, and
  Liquibase migrations as authoritative.
- Treat tests as supporting evidence, not a substitute for reading the current
  implementation.
- Treat `README.md`, comments, TODOs, branch names, and old documentation as
  context only when they conflict with executable code.
- Do not infer backend support from frontend models, mocks, forms, or planned
  features.
- Do not describe an endpoint as usable merely because a DTO or unfinished
  service exists.

## Coding Conventions

- Use Java 17 and the existing Spring Boot patterns.
- Keep HTTP handling in controllers and business rules in services.
- Use DTOs at API boundaries; do not expose JPA entities directly.
- Preserve package-by-domain organization and existing naming conventions.
- Prefer constructor injection. Lombok may be used consistently with nearby
  code.
- Put ownership and authorization checks in reusable services rather than
  duplicating them in controllers.
- Mark service operations transactional where they mutate related state or
  depend on lazy JPA relationships.
- Extend `GlobalExceptionHandler` when adding a domain exception so the API
  returns a deliberate status and response shape.
- Add or update tests for changed business behavior, authorization, API
  contracts, persistence, and OpenAPI exposure.
- Do not casually remove legacy endpoints. Confirm frontend usage first and
  provide a migration path when replacing an API.

## Product Domain Rules

- Backend-supported product types are currently `COURSE`, `DOWNLOAD`, and
  `CONSULTATION`.
- Backend-supported product statuses are `DRAFT`, `PUBLISHED`, and `HIDDEN`.
- `MEMBERSHIP` is not a persisted backend product type. Do not add membership
  fields to generic product DTOs or claim membership support without a complete,
  reviewed backend contract and migration.
- Products use JPA `TABLE_PER_CLASS` inheritance. Each concrete product table
  contains the shared Product fields.
- Product-type behavior is routed through `ProductTypeHandler`; add a complete
  handler, converter, DTO, entity, repository, migration, tests, and API docs
  when introducing a product type.
- Product creation and mutation require `CREATOR` or `ADMIN`.
- Creators may mutate only their own products. Admins may act across owners.
- Admin-created products must be assigned to a user whose single role is
  `CREATOR`.
- Course and Download products contain ordered sections. Only Course sections
  contain lessons.
- Supported lesson types are `VIDEO`, `ARTICLE`, and `QUIZ`.
- Download files belong to Download sections. Do not return permanent file URLs
  as public product data.

## Authentication and Authorization Rules

- Authentication is stateless and JWT-based. Access and refresh tokens are
  handled through cookies.
- CSRF is enforced by the custom `CsrfFilter`; Spring Security's built-in CSRF
  mechanism is disabled intentionally.
- `/api/auth/**`, `/testEndpoint`, public Product GET routes, and calendar
  provider discovery have URL-level public access. Other routes require
  authentication unless explicitly configured otherwise.
- Method-level role rules use `@PreAuthorize`; preserve them when refactoring
  controllers.
- Roles are `ADMIN`, `CREATOR`, and `USER`, and the database enforces one role
  per user.
- Do not trust caller-provided owner or user IDs for authorization. Resolve the
  authenticated user through `CurrentUserService` and enforce ownership.
- The development role-switch endpoint must remain controlled by
  `app.dev-role-switch.enabled`; do not make it an unconditional production
  capability.

## Entitlement and Content-Access Rules

- An entitlement is unique per user and product and has `ACTIVE` or `REVOKED`
  status.
- Entitlement sources are `FREE_ENROLLMENT`, `PURCHASE`, and `ADMIN_GRANT`.
- Self-enrollment is supported only for published products whose price is null
  or not greater than zero.
- Admins and product owners can access protected product content without an
  entitlement. Other users require an active entitlement.
- Public Product responses may include published Product metadata, but protected
  Course content and Download URLs must be removed.
- Download access must return a short-lived authorized URL through the
  entitlement file-access flow.
- Deleting a product deletes its entitlement records before deleting the
  product.

## Database and Migration Rules

- PostgreSQL is the production database. H2 is used by tests.
- Hibernate schema generation is disabled with `ddl-auto: none`.
- Every schema change must be an additive Liquibase migration included from
  `db.changelog-master.xml`.
- Never edit a migration that may already have run in an environment. Add a new
  numbered migration instead.
- Keep database constraints aligned with JPA mappings and service invariants.
- Include rollback or safe forward-repair thinking for destructive schema
  changes.
- Add integration tests when a migration changes constraints, relationships,
  inheritance behavior, or query assumptions.

## API and Swagger Rules

- Springdoc-generated OpenAPI is the authoritative reference for current paths,
  methods, parameters, payload schemas, and response codes.
- Swagger/OpenAPI is disabled by default and enabled with the `docs` Spring
  profile through `application-docs.yml`.
- Keep annotations and interfaces under `controller/docs` synchronized with
  controller and DTO changes.
- Add operation-level documentation for new endpoints and keep security/error
  responses accurate.
- Swagger documents the exact wire contract. Docusaurus should explain domain
  concepts, workflows, permissions, integration guidance, architecture, and
  current limitations; do not manually duplicate every schema there.

## Configuration and Secret Safety

- Use environment variables for credentials, signing keys, provider secrets,
  and deployment-specific values.
- Never add new secrets or real credentials to tracked configuration, tests,
  logs, documentation, or examples.
- Avoid logging tokens, passwords, cookie values, OAuth codes, private file
  URLs, or full sensitive request objects.
- Changes to CORS, cookies, token expiry, CSRF, Swagger access, or public route
  matchers are security-sensitive and require targeted tests.

## Validation Commands

Run from this repository:

```bash
./mvnw test
```

For a packaging check:

```bash
./mvnw package
```

For local development:

```bash
./mvnw spring-boot:run
```

Run the relevant focused tests while iterating, then the full test suite before
finishing whenever practical. If validation cannot run, report why.

Current checkout verification: `./mvnw test` passes on Java 17. The local
machine's Java 25 default is not compatible with the current compiler/Lombok
setup, so select a Java 17 runtime before running Maven.

## Documentation Synchronization

Every backend change must include a documentation-impact review before the task
is considered complete.

Update the sibling `../video-platform-UI-docs` repository when a backend change
affects any of the following:

- business rules, user-visible behavior, workflows, or limitations
- API capabilities, request or response meaning, or integration guidance
- authentication, authorization, roles, ownership, or entitlement behavior
- database concepts maintainers need to understand
- external integrations, file handling, deployment, or configuration
- architecture, package ownership, maintenance conventions, or setup

Also update this repository's `PROJECT_CONTEXT.md` when architecture, major
domain behavior, persistence strategy, security behavior, integrations, or
known limitations change.

For API changes:

1. Update controller behavior, DTOs, tests, and Springdoc/OpenAPI together.
2. Update Docusaurus only where the conceptual API documentation, business
   meaning, permissions, workflow, or examples changed.
3. Link readers to Swagger for the exhaustive live endpoint and schema
   reference instead of copying it manually.

If the documentation repository is unavailable or outside the task's writable
scope, explicitly list the required documentation changes in the final report.
If no documentation changes are needed, explicitly state that conclusion.

Do not mark a feature complete merely because the backend compiles. Confirm the
frontend contract when relevant and keep the implementation, OpenAPI,
`PROJECT_CONTEXT.md`, and Docusaurus documentation consistent.
