---
id: "0001"
title: "Constitution layered DTO Flyway TDD"
stage: "constitution"
date: "2026-03-22T17:34:54Z"
surface: "cursor-chat"
model: "unknown"
feature: "none"
branch: "main"
user: "unknown"
command: "/sp.constitution"
labels: [constitution, governance, tdd, flyway, dto]
links:
  spec: "n/a"
  ticket: "n/a"
  adr: "n/a"
  pr: "n/a"
files:
  - ".specify/memory/constitution.md"
  - ".specify/templates/plan-template.md"
  - ".specify/templates/tasks-template.md"
  - ".specify/templates/spec-template.md"
tests:
  - "none (constitution and template updates only)"
---

## Prompt

/sp.constitution Layered Architecture
MUST follow Controller → Service → Repository; no business logic in controllers
DTO Enforcement
MUST NOT expose JPA entities via APIs; MUST use Request/Response DTOs
Database Management
MUST manage schema changes via Flyway migrations only
TDD Mandatory
MUST follow Test → Fail → Implement → Refactor; no code without testsLayered Architecture
MUST follow Controller → Service → Repository; no business logic in controllers
DTO Enforcement
MUST NOT expose JPA entities via APIs; MUST use Request/Response DTOs
Database Management
MUST manage schema changes via Flyway migrations only
TDD Mandatory
MUST follow Test → Fail → Implement → Refactor; no code without tests

## Response snapshot

Adopted **Spring Boot SDD POC Constitution** v1.0.0 with four principles (layered architecture, DTO enforcement, Flyway-only schema, mandatory TDD), plus Additional Constraints, Development Workflow, and Governance. Synced `plan-template.md` Constitution Check gates, `tasks-template.md` test requirements, and `spec-template.md` compliance line. PHR recorded under `history/prompts/constitution/`.

## Outcome

- ✅ Impact: Project constitution ratified; templates aligned for `/sp.plan`, `/sp.tasks`, `/sp.specify` outputs.
- 🧪 Tests: No code/tests run (documentation-only change).
- 📁 Files: constitution.md + three templates updated.
- 🔁 Next prompts: `/sp.plan` on active feature; `/sp.analyze` for constitution checks.
- 🧠 Reflection: Roam MCP not present; gates reference concrete paths (Flyway dir, constitution file).

## Evaluation notes (flywheel)

- Failure modes observed: none
- Graders run and results (PASS/FAIL): n/a
- Prompt variant (if applicable): n/a
- Next experiment (smallest change to try): Run `/sp.analyze` on next feature branch to validate gates.
