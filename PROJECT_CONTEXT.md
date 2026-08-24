# PROJECT_CONTEXT.md

Generated from repository state:

- Branch: `main`
- Commit: `f110de5`
- Last reviewed: 2026-08-22

## How to Use This Document

This document bootstraps a new Codex context for the Video Platform backend.
Read `AGENTS.md` first.

Treat the current implementation as the source of truth when this document,
the README, frontend assumptions, Swagger text, or external documentation
disagrees with the code. Update this file after significant architectural,
security, persistence, integration, or domain changes.

## Repository Relationships

The platform is split across three sibling repositories:

- `video-platform`: this Spring Boot backend.
- `video-platform-ui`: the React and TypeScript frontend.
- `video-platform-UI-docs`: the Docusaurus product and developer documentation.

The backend owns persisted business rules and API contracts. The frontend owns
the implemented UI and may contain local mocks or models for backend-pending
features. Docusaurus describes the verified product and developer experience.

When a feature spans repositories, inspect all relevant implementations. A
frontend model does not prove backend support, and a backend endpoint does not
prove that a user-facing workflow is exposed in the UI.

## Product Overview

The application supports a creator commerce and learning platform. Users can
register and authenticate, Creators can author supported products, Admins can
manage users and products, and authenticated users can receive entitlements to
protected product content.

Current roles:

- `ADMIN`: platform-wide administration and cross-owner Product management.
- `CREATOR`: owns and authors Products.
- `USER`: consumes Products and maintains a library through entitlements.

The database enforces one role per user.

Current backend Product types:

- `COURSE`: ordered sections containing `VIDEO`, `ARTICLE`, or `QUIZ` lessons.
- `DOWNLOAD`: ordered sections containing downloadable files.
- `CONSULTATION`: duration, meeting method, location, buffer, capacity, message,
  policy, and connected-calendar information.

Current Product statuses are `DRAFT`, `PUBLISHED`, and `HIDDEN`.

`MEMBERSHIP` is not implemented as a backend Product type. Frontend Membership
contracts or mocks must not be treated as production backend capability.

## Technology and Runtime

- Java 17
- Spring Boot 3.4.x
- Spring MVC and Spring Security
- Spring Data JPA with Hibernate
- PostgreSQL in deployed environments
- H2 for tests
- Liquibase for schema migrations
- JWT access and refresh authentication
- Springdoc OpenAPI and Swagger UI
- DigitalOcean Spaces through AWS SDK S3
- SendGrid email
- Google authentication and calendar integration infrastructure
- Maven Wrapper for build and test execution
- Dokku deployment on DigitalOcean through GitHub Actions

The application entry point is
`src/main/java/com/myproject/video/video_platform/VideoPlatformApplication.java`.

## Architecture Summary

The codebase uses a conventional layered Spring structure:

1. Controllers receive HTTP requests and return DTOs.
2. Services enforce business rules, authorization, transactions, and external
   integration behavior.
3. Repositories provide JPA persistence access.
4. Entities define persistence relationships and constraints.
5. Converters map between entities and API DTOs.
6. Liquibase migrations define the actual database evolution.

Cross-cutting behavior includes:

- `SecurityConfig` for URL security, CORS, stateless sessions, JWT, and CSRF
  filter placement.
- `GlobalExceptionHandler` for API error mapping.
- `OpenApiDefaultResponsesConfig` for shared OpenAPI authentication responses.
- `CurrentUserService` for extracting the authenticated user ID.
- `ProductAuthorizationService` for Product ownership and Admin behavior.
- `ProductContentAccessService` for filtering protected Product content.

## Authentication and Security

Authentication endpoints are under `/api/auth` and include registration, email
verification, login, logout, token refresh, and Google sign-in.

The backend uses stateless Spring Security with JWT-based access. Access and
refresh tokens are managed as cookies. The refresh endpoint reads the
`REFRESH_TOKEN` cookie and rotates or reissues authentication through
`AuthService`.

Spring Security's built-in CSRF mechanism is disabled because a custom
`CsrfFilter` provides the application's CSRF behavior. JWT authentication runs
before that filter.

URL-level public access currently includes:

- `/api/auth/**`
- `/testEndpoint`
- GET requests under `/api/products/**`
- GET `/api/calendars/providers`

All other requests require authentication unless method-level or future URL
configuration says otherwise. Controllers also use `@PreAuthorize` for role
checks.

Security-sensitive configuration includes allowed CORS origins, cookie
behavior, token expiry, JWT signing, OAuth state signing/encryption, provider
credentials, and Swagger access. Deployment secrets should come from the
environment and must not be copied into documentation or logs.

The development role switch is exposed through `/api/user/dev/role` and gated
by `app.dev-role-switch.enabled`. Its configuration must be reviewed before any
production deployment.

## User and Role Model

`User` uses a UUID primary key and stores identity, profile, onboarding, auth
provider, Product ownership, social links, and role relationships.

Supported role values are `ADMIN`, `CREATOR`, and `USER`. Although the JPA model
uses a set and a join table, migration 31 adds a unique index on `user_id`, so a
user can have only one role.

Admin role updates enforce a last-admin safety rule: the final Admin cannot be
demoted. Admin role and Product mutations are recorded in the Admin audit log.

## Product Persistence and Strategy

`Product` is an abstract JPA entity using `InheritanceType.TABLE_PER_CLASS`.
Concrete Product tables repeat shared fields including ID, name, description,
image, type, status, owner, price, customer count, and timestamps.

Product behavior is dispatched through `ProductTypeHandler` implementations:

- `CourseProductHandler`
- `DownloadProductHandler`
- `ConsultationProductHandler`

Converters map each concrete Product type to its API DTO. The generic Product
service resolves the correct handler, applies ownership rules, records Admin
actions where applicable, coordinates entitlement cleanup on deletion, and
supports search and summary reads.

Creator Product creation ignores an attempted foreign owner and assigns the
authenticated Creator. Admin Product creation requires an explicit owner whose
single role is `CREATOR`. Product mutation requires ownership or Admin access.

PATCH updates merge supported shared/type-specific fields into the concrete DTO.
Course and Download details are intentionally excluded from generic Product
PATCH updates because sections, lessons, and files have separate authoring
flows.

## Product Authoring

Canonical Product authoring is nested under the Product:

- `/api/products/{productId}/sections`
- `/api/products/{productId}/sections/{sectionId}`
- `/api/products/{productId}/sections/{sectionId}/lessons`
- `/api/products/{productId}/sections/{sectionId}/lessons/{lessonId}`
- `/api/products/{productId}/sections/{sectionId}/files`

These mutations require `CREATOR` or `ADMIN`, verify ownership, and verify that
child resources belong to the parent IDs in the path.

Course sections support lessons. Download sections support files. Services
reject invalid cross-type operations rather than silently accepting them.

Legacy Course and generic file endpoints still exist. Confirm frontend usage
before removing them. Prefer canonical nested routes for new integrations.

## Course Quizzes

A Course lesson can own one Quiz. Quiz authoring is under
`/api/lessons/{lessonId}/quiz` and requires Creator or Admin access. Authenticated
play and submission endpoints are `/play` and `/submit` beneath that path.

Supported question types are:

- `multiple_choice_single`
- `multiple_choice_multi`
- `true_false`

Quiz persistence includes quizzes, ordered questions, options, and user
attempts. Passing score is constrained to 0-100 and question points must be
positive. Authoring validation and submission evaluation live in dedicated
services.

## Entitlements and Protected Content

`ProductEntitlement` records access separately from concrete Product tables.
Each user/Product pair is unique.

Statuses:

- `ACTIVE`
- `REVOKED`

Sources:

- `FREE_ENROLLMENT`
- `PURCHASE`
- `ADMIN_GRANT`

Current public entitlement API capabilities:

- enroll the authenticated user in a free published Product
- list the authenticated user's active library, optionally filtered by Product
  type
- check access to a Product
- obtain an authorized Download file URL

Self-enrollment rejects non-published Products and Products with a positive
price. Purchase and Admin-grant source values exist in the model, but this
repository does not currently expose a complete payment or Admin grant/revoke
workflow.

Admins and Product owners bypass entitlement checks for content access. Other
users require an active entitlement. Public Product reads remove protected
Course lesson body/video content and Download URLs. Draft and Hidden Products
are unavailable to callers who lack owner/Admin access.

Download delivery resolves the file through its Product and section, verifies
content access, and returns a time-limited URL rather than exposing a permanent
storage URL in Product payloads.

## Commerce Foundation

The backend now persists one-time commerce Orders, immutable Order-item
snapshots, payment attempts, and idempotent processed payment events. Checkout
is authenticated, server-priced, EUR-only, limited to 20 unique published paid
Products, and restricted to Products owned by one Creator.

Current commerce endpoints are:

- `POST /api/commerce/checkout-sessions` with an `Idempotency-Key` header
- `GET /api/commerce/orders/{orderId}` for the buyer or an Admin

`PaymentGateway` is the provider boundary. The current implementation includes
only a fake gateway under the `dev` and `test` profiles. Commerce and fake
simulation are disabled by default. When explicitly enabled for development,
an Admin can simulate `PAID`, `FAILED`, and `REFUNDED` outcomes through
`POST /api/dev/commerce/orders/{orderId}/simulate`.

A paid event grants `PURCHASE` entitlements linked to the originating Order
items. A full refund revokes only those linked purchase entitlements. Duplicate
events and repeated checkout requests are idempotent. Products with active
purchase access or an unexpired pending checkout cannot be deleted.

Creator-only reporting APIs now expose Sales summaries, the Order ledger and
Order detail, Customer list/detail read models, and an Analytics overview from
these Commerce and entitlement records. Reporting is scoped through the
authenticated Creator ID, uses UTC calendar periods from an injected `Clock`,
and returns `404` for cross-Creator Order or Customer detail lookups. Admin and
User roles are deliberately rejected.

Sales revenue is retained paid revenue: only Orders currently in `PAID` state
contribute. Fully refunded Orders are reported separately. Ledger periods use
Order creation time, while revenue, refunds, and failures use their respective
event timestamps. Customer relationships include paid/refunded buyers and
users with free, purchased, or manually granted Product entitlements. Analytics
returns no fabricated Membership values while that domain is absent.

This is not production payment processing yet. A future Stripe adapter must
create hosted sessions, verify webhook signatures, normalize provider events,
and then call the existing payment-event processor. Subscriptions, partial
refunds, taxes, payouts, and coupons remain outside this foundation.

## Consultation Calendars

Calendar endpoints expose provider discovery, connection initiation, OAuth
callback handling, connection listing, and disconnection.

Provider clients exist for Google, Microsoft, and iCloud. The current OAuth
callback path and provider behavior must be checked before claiming equal
support across providers; the implementation throws explicit errors for
unsupported provider operations.

Calendar connection state is tied to the authenticated user and protected by
signed state. Stored provider token material is handled through the calendar
crypto configuration.

## Admin Capabilities

Endpoints under `/api/admin` require `ADMIN` and currently support:

- paginated user search and role filtering
- replacing a user's single role
- paginated Product search by text, owner, type, and status
- paginated Admin audit-log filtering

Admins may also create and mutate Products through the standard Product APIs,
subject to Admin-specific owner rules. Audit records capture relevant user-role
and Product actions.

## Persistence Model

Liquibase is authoritative for schema evolution. The master changelog includes
numbered SQL migrations for:

- users, roles, single-role enforcement, verification tokens, and refresh
  tokens
- user profile and social-link data
- Course, Download, and Consultation Product tables
- Course and Download sections and their child content
- Quiz definitions, options, and attempts
- connected calendars
- search indexes
- Admin audit logs
- Product entitlements
- one-time commerce Orders, Order items, payment attempts, payment events, and
  purchase-entitlement references
- Creator/payment-date, customer-aggregation, and entitlement-relationship
  reporting indexes

Hibernate uses `ddl-auto: none`; adding or changing an entity does not update
the deployed schema automatically. Every persistence change needs a new
Liquibase migration and corresponding tests.

## OpenAPI and Swagger

The project uses Springdoc. OpenAPI annotations are largely separated into
interfaces under `controller/docs`, which controllers implement.

Swagger and `/v3/api-docs` are disabled by default. The `docs` Spring profile
enables them and adds a dedicated HTTP Basic security chain. Swagger's submit
methods are disabled, so the UI is configured as a read-only contract browser.

Use Swagger/OpenAPI as the exhaustive source for current endpoint paths,
parameters, schemas, and documented responses. Use Docusaurus for durable
explanations of:

- domain concepts and business rules
- role, ownership, and entitlement behavior
- architecture and persistence decisions
- integration workflows and examples
- current limitations and cross-repository behavior

This split avoids maintaining a second handwritten copy of every DTO while
still giving developers the context Swagger cannot express well.

## External Services and Deployment

DigitalOcean Spaces stores uploaded assets. Presigned upload and confirmation
flows exist for files, and protected Download delivery creates an authorized
download URL.

SendGrid is used for email flows such as account verification. Google sign-in
uses Google's token verification libraries.

The repository deploys through `.github/workflows/ci-cd-dokku.yml`. Pull
requests and pushes to `main` run Maven tests. A successful push to `main` is
then deployed to Dokku using repository secrets.

The README primarily documents deployment and local startup. It does not fully
describe current architecture or domain behavior; this file and the codebase
are better bootstrap references.

## Current Feature State

Implemented and server-backed:

- local registration, email verification, login, logout, and refresh
- Google sign-in
- authenticated user profile and social-link updates
- single-role user model and Admin role management
- Course, Download, and Consultation Product CRUD
- Product ownership and Admin cross-owner rules
- Product search and Product summaries
- canonical section, lesson, and Download file authoring
- Course Quiz authoring, play, submission, and attempt persistence
- calendar connection infrastructure
- Admin Product/user search and audit logs
- free Product enrollment, user library, access checks, protected Product
  responses, and authorized Download delivery
- Springdoc-generated OpenAPI under the `docs` profile
- provider-neutral one-time commerce Order persistence, idempotent checkout
  orchestration, fake dev/test payment transitions, paid entitlement creation,
  and full-refund entitlement revocation
- Creator-only Sales summary, Order ledger/detail, Customer list/detail, and
  Analytics overview read APIs backed by Commerce and entitlements

Not currently implemented as complete backend capabilities:

- Membership Products, membership content, subscriptions, or member access
- production checkout through Stripe or another real payment provider
- public paid purchase completion in the frontend
- partial refunds, payment retries, taxes, coupons, marketplace payouts, or
  provider-driven dispute handling
- an exposed Admin entitlement grant/revoke workflow
- Creator dashboard aggregate APIs represented by frontend-only mocks/contracts
- Membership/waitlist Customer relationships, Membership analytics, editable
  Customer notes/tags, and reporting exports
- Storefront configuration and public Storefront backend contracts represented
  by frontend-only mocks/contracts
- Product Landing Page configuration persistence represented by frontend-only
  contracts
- generalized asset lifecycle for all Membership or rich media use cases

## Known Risks and Maintenance Notes

- Tracked application configuration contains security-sensitive defaults and
  values. Do not copy them elsewhere. Move secrets and signing material to
  environment-managed configuration and rotate exposed credentials as a
  separate security task.
- Current authentication logging can include registration request fields and
  serialized User data, including password material. Redact these logs and avoid
  logging sensitive DTOs or entities.
- The development role switch defaults to enabled unless overridden by the
  environment; production configuration must disable it deliberately.
- Swagger's docs-profile user is currently configured in code and should be
  replaced with environment-managed credentials before broader exposure.
- `GlobalExceptionHandler` has a generic exception handler that maps unexpected
  exceptions to HTTP 400 and includes the exception message. This can hide
  server failures and disclose internal detail.
- Both Spring MVC and WebFlux dependencies are present. The application is
  structured primarily as MVC; confirm the need for WebFlux before expanding
  reactive usage.
- The Spring Data JPA dependency has an explicit version different from the
  Spring Boot parent-managed line. Avoid independent version changes without a
  compatibility check.
- Legacy Product/Course/file endpoint shapes coexist with canonical nested
  authoring routes.
- Product summary/search endpoints should be reviewed carefully for publication
  and visibility semantics before being used as a public catalogue contract.
- Provider classes existing in the repository do not necessarily mean every
  calendar provider has a complete production OAuth flow.
- The current compiler/Lombok setup fails when Maven runs under the local Java
  25 default. The project targets Java 17 and the test suite passes under Java
  17.

## Validation and Documentation Workflow

Use the Maven Wrapper:

```bash
./mvnw test
./mvnw package
```

For local startup:

```bash
./mvnw spring-boot:run
```

For any backend feature or behavior change:

1. Update implementation, tests, DTOs, migrations, and OpenAPI as applicable.
2. Review `PROJECT_CONTEXT.md` for architecture or domain changes.
3. Review `../video-platform-ui` for contract compatibility when the endpoint is
   consumed by the frontend.
4. Review `../video-platform-UI-docs` for product and developer documentation
   impact.
5. Update the affected Docusaurus pages when behavior, business rules,
   permissions, integration guidance, architecture, tables, or limitations
   changed.
6. If no Docusaurus change is required, explicitly state that in the final
   implementation report.

Swagger remains the detailed API contract. Docusaurus should link to Swagger
and document the meaning and use of the backend rather than duplicating every
generated endpoint schema.
