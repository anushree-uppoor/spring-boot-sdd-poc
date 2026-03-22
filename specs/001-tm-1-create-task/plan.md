# Implementation Plan: TM-1 Create Task

**Branch**: `001-tm-1-create-task` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/001-tm-1-create-task/spec.md`

**Note**: This template is filled in by the `/sp.plan` command. See `.cursor/commands/sp.plan.md` for the execution workflow.

## Summary

Deliver **POST /v1/tasks** so clients can create a **Task** with required **title** (≤100 chars) and optional **description** (≤255 chars), persisted in **H2** via **JPA**, with **201** + **TaskResponse** (id, title, description, **PENDING** status, **createdAt**). Invalid input returns **400** with field-level errors per project error shape. Implementation follows **Controller → Service → Repository**, **Request/Response DTOs** with **`@Valid`**, **Flyway** for the `tasks` table, and **TDD** (service unit tests, `MockMvc` controller tests, optional `@DataJpaTest` or slice test if persistence logic warrants).

The existing repo root is a skeleton **Spring Boot 4.0.x** app under `com.sdd.poc`. This feature **introduces `com.example.tasktracker`** per `.specify/memory/constitution.md` and `.cursor/rules/active-rules/package-structure.mdc`: add a **TaskTracker** application entry (or move the main class) so component scanning covers the new package tree.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot 4.0.x, Spring Web MVC, Spring Data JPA, Spring Validation, Flyway, H2  
**Storage**: H2 (file or in-memory per `application` config); relational schema via Flyway `src/main/resources/db/migration`  
**Testing**: JUnit 5, Mockito, `spring-boot-starter-webmvc-test` (`MockMvc`), `@DataJpaTest` where useful  
**Target Platform**: JVM / local server (demo POC)  
**Project Type**: Single backend (Maven)  
**Performance Goals**: Spec success criteria — confirm success within **5s** for **95%** of valid creates under normal local/dev conditions (non-load-test).  
**Constraints**: Constitution — no entities on the wire; Flyway-only DDL authority; layered architecture; tests before behavior changes.  
**Scale/Scope**: Single-feature slice; no auth/multi-tenant in this spec.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Per `.specify/memory/constitution.md`, confirm before proceeding:

- **Layered architecture:** Design keeps **Controller → Service → Repository**; no business logic or repository access planned in controllers.
- **DTOs:** API shapes use **Request/Response DTOs**; no exposure of JPA entities at HTTP boundaries.
- **Schema:** Any DB change is delivered as a **Flyway** migration under `src/main/resources/db/migration` (no ad-hoc DDL as the source of truth).
- **TDD:** Plan includes **failing tests first** for new behavior (which layers: unit / `MockMvc` / integration) before implementation tasks.

If any item cannot be met, document justification in **Complexity Tracking** below.

### Post-design (Phase 1) re-check

- **Layering:** `TaskController` → `TaskService` → `TaskRepository` only.  
- **DTOs:** `CreateTaskRequest`, `TaskResponse` + JPA `Task` entity; mapping in service.  
- **Schema:** Initial `tasks` table via Flyway script (aligned with [data-model.md](./data-model.md)).  
- **TDD:** Tests listed in [quickstart.md](./quickstart.md) verification and implied `/sp.tasks` follow-up.

## Project Structure

### Documentation (this feature)

```text
specs/001-tm-1-create-task/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/           # Phase 1 (OpenAPI)
└── tasks.md             # Phase 2 (/sp.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/example/tasktracker/
├── TaskTrackerApplication.java   # @SpringBootApplication (new or moved from com.sdd.poc)
├── controller/
│   └── TaskController.java
├── service/
│   └── TaskService.java
├── repository/
│   └── TaskRepository.java
├── entity/
│   └── Task.java
├── dto/
│   ├── request/
│   │   └── CreateTaskRequest.java
│   └── response/
│       └── TaskResponse.java
├── exception/                    # GlobalExceptionHandler, ErrorResponse, etc. as needed
└── config/                       # Optional beans

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__create_tasks_table.sql

src/test/java/com/example/tasktracker/
├── TaskTrackerApplicationTests.java   # Context load (adjust if main moved)
├── controller/
│   └── TaskControllerTest.java
└── service/
    └── TaskServiceTest.java
```

**Structure Decision**: Single Maven module; packages under **`com.example.tasktracker`** per constitution. Deprecate or remove the empty **`com.sdd.poc`** bootstrap once **`TaskTrackerApplication`** is the entry point (avoid two competing `@SpringBootApplication` classes).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

_No unjustified violations._ Package migration from `com.sdd.poc` to `com.example.tasktracker` is required alignment with governance, not an exception.
