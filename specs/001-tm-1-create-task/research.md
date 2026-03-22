# Research: TM-1 Create Task

Consolidated decisions for Phase 0 (no open **NEEDS CLARIFICATION** from Technical Context).

## 1. Persistence and schema tooling

**Decision:** Use **Spring Data JPA** + **Flyway** + **H2** for this POC.

**Rationale:** Matches `.specify/memory/constitution.md` (Flyway-only schema authority) and existing design input [schema.sql](../../.specify/design-inputs/data-models/schema.sql). H2 keeps local/demo friction low.

**Alternatives considered:** Spring JDBC without JPA (more manual mapping, conflicts with common Spring Boot layering in rules); `ddl-auto=update` (rejected — not Flyway-authoritative).

## 2. Whitespace on title and description

**Decision:** **Trim** leading/trailing whitespace on title and description in the **service** before validation persistence; treat all-whitespace title as invalid (equivalent to blank after trim).

**Rationale:** Matches spec edge case “normalized or rejected consistently”; trimming gives predictable UX and keeps `@NotBlank` meaningful on trimmed values.

**Alternatives considered:** Reject any leading/trailing spaces without trim (harsher UX); trim only in controller (violates “thin controller” preference — service owns normalization).

## 3. API error shape for validation failures

**Decision:** Use **`MethodArgumentNotValidException`** handling in a **`@RestControllerAdvice`** returning JSON with **`statusCode`**, **`message`**, **`errors[]`** `{ field, message }`, **`timestamp`**, **`path`** — aligned with `.cursor/rules/active-rules/api-design.mdc` / `exception-handling.mdc`.

**Rationale:** Satisfies FR-004 (field-level feedback) and keeps clients consistent for future endpoints.

**Alternatives considered:** RFC 7807 Problem Details (not required by current rules); raw Spring default JSON (inconsistent with documented error contract).

## 4. Timestamp type and serialization

**Decision:** Store **`created_at`** as **`TIMESTAMP`** in DB; expose **`createdAt`** in API as **ISO-8601** string via **`Instant`** or **`OffsetDateTime`** in Java with default Jackson serialization.

**Rationale:** OpenAPI `date-time` and cross-platform clients expect ISO-8601.

**Alternatives considered:** Epoch millis in API (poor human debuggability); `LocalDateTime` without zone (ambiguous for APIs — avoid if possible; `Instant` preferred).

## 5. Maven coordinates and starters

**Decision:** Add **`spring-boot-starter-data-jpa`**, **`spring-boot-starter-validation`**, **`flyway-core`**, **`flyway-database-h2`**, **`h2`** (scopes/runtime per Spring Boot BOM). Keep **`spring-boot-starter-webmvc`** as the web stack.

**Rationale:** Minimal set for REST + JPA + migrations + validation; matches tech-stack rules.

**Alternatives considered:** PostgreSQL for POC (heavier setup; H2 sufficient for spec).
