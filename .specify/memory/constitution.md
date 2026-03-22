<!--
Sync Impact Report
- Version: placeholder template → 1.0.0 (initial adoption)
- Principles: filled — Layered Architecture, DTO Enforcement, Database Management, TDD Mandatory
- Added sections: Additional Constraints, Development Workflow (template Section 2 & 3)
- Removed sections: unused sixth principle slot (template had 6 placeholders; project adopts 4)
- Templates: .specify/templates/plan-template.md ✅ | tasks-template.md ✅ | spec-template.md ✅
- Follow-up TODOs: none
-->

# Spring Boot SDD POC Constitution

## Core Principles

### Layered Architecture

MUST follow **Controller → Service → Repository** only. Controllers handle HTTP mapping,
validation delegation (`@Valid`), and service calls; they MUST NOT contain business rules,
orchestrate transactions beyond what the framework implies at the boundary, or call
repositories directly. Services MUST hold business logic and coordinate repositories.
Repositories MUST perform persistence only. Skipping a layer or pushing domain logic into
controllers is a constitution violation.

**Rationale:** Clear boundaries keep behavior testable and changes localized.

### DTO Enforcement

MUST NOT expose **JPA entities** (or other persistence-centric types) as public API request
or response bodies. MUST use **Request DTOs** for input (with validation annotations) and
**Response DTOs** for output. Mapping between DTOs and entities MUST occur at the service
boundary (manual mapping or small mappers), not in controllers.

**Rationale:** Stable API contracts and no leakage of persistence shape to clients.

### Database Management

MUST apply **relational schema changes only through Flyway** versioned migrations in the
project migration directory (e.g. `src/main/resources/db/migration`). MUST NOT rely on
ad-hoc DDL in code, undocumented manual schema edits in shared environments, or
Hibernate `ddl-auto` (or equivalent) for production-destined schema authority.

**Rationale:** Reproducible, reviewable schema history across environments.

### TDD Mandatory

MUST follow **Test → Fail → Implement → Refactor**: add or extend a failing automated test
that expresses the required behavior, confirm it fails for the right reason, implement the
minimum code to pass, then refactor. MUST NOT introduce or change production behavior without
accompanying automated tests appropriate to the layer (unit tests with mocks for services;
controller tests with `MockMvc`; integration tests where persistence is in scope).

**Rationale:** Regressions are caught early; design stays testable.

## Additional Constraints

- **Stack:** Java 17+, Spring Boot, Spring Data JPA, Maven — see `.cursor/rules/active-rules/tech-stack.mdc`.
- **Package layout:** `com.example.tasktracker` with `controller`, `service`, `repository`,
  `entity`, `dto` (`request` / `response`), `exception`, `config` per
  `.cursor/rules/active-rules/package-structure.mdc`.
- **REST contracts:** When an OpenAPI contract exists for the API surface (e.g.
  `design-inputs/api-contracts/task-tracker.yaml`), implementation MUST match it.

## Development Workflow

- **Spec-first:** Use `/sp.specify` before implementation; one spec folder per branch per
  `.cursor/rules/spec-folder-management.mdc`.
- **Planning:** `plan.md` MUST include a **Constitution Check** gate satisfied before
  implementation; unjustified violations go in **Complexity Tracking**.
- **Analysis:** `/sp.analyze` treats constitution conflicts as **CRITICAL** and requires
  spec/plan/task or code adjustment—not silent waiver.

## Governance

This constitution supersedes informal coding habits when they conflict. **Amendments** require
a pull request with explicit rationale, semantic version bump (MAJOR for incompatible
principle removal/redefinition, MINOR for new or materially expanded principles, PATCH for
clarifications only), and reviewer approval. **Compliance:** PR authors note layering, DTO,
Flyway, and TDD impact where relevant; reviewers verify. **Operational detail:** See
`.cursor/rules/guidelines.md` and `.cursor/rules/active-rules/` for day-to-day patterns.

**Version**: 1.0.0 | **Ratified**: 2026-03-22 | **Last Amended**: 2026-03-22
