---
description: "Task list for TM-1 Create Task (001-tm-1-create-task)"
---

# Tasks: TM-1 Create Task

**Input**: Design documents from `/specs/001-tm-1-create-task/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/openapi.yaml](./contracts/openapi.yaml), [quickstart.md](./quickstart.md)

**Tests**: Per `.specify/memory/constitution.md` (**TDD Mandatory**), failing **TaskServiceTest** and **TaskControllerTest** MUST exist and fail for the right reasons **before** production classes `TaskService` and `TaskController` satisfy them.

**Organization**: Single P1 user story (US1); phases are Setup → Foundational → US1 (tests then implementation) → Polish.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no ordering dependency)
- **[US1]**: User Story 1 — Create a task with a title
- Every task includes at least one concrete file path

## Path Conventions

Maven layout at repository root: `src/main/java`, `src/test/java`, `src/main/resources`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependencies, configuration, and Spring Boot entry point under `com.example.tasktracker`.

- [x] T001 Add Spring Data JPA, Validation, Flyway, and H2 dependencies to `pom.xml` (see [plan.md](./plan.md) Technical Context)
- [x] T002 Configure datasource, JPA, and Flyway in `src/main/resources/application.yml` (H2; disable ddl-auto as schema authority — use Flyway only)
- [x] T003 Replace skeleton bootstrap: remove `src/main/java/com/sdd/poc/PocApplication.java`, add `src/main/java/com/example/tasktracker/TaskTrackerApplication.java`, and replace `src/test/java/com/sdd/poc/PocApplicationTests.java` with `src/test/java/com/example/tasktracker/TaskTrackerApplicationTests.java` (context-load smoke test only)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Database migration, persistence model, repository, and API error envelope — **required before US1 tests can compile against real types**.

**⚠️ CRITICAL**: No US1 production implementation until **after** failing US1 tests (Phase 3) are written.

- [x] T004 Add Flyway migration `src/main/resources/db/migration/V1__create_tasks_table.sql` per [data-model.md](./data-model.md) and `.specify/design-inputs/data-models/schema.sql`
- [x] T005 Add JPA entity `src/main/java/com/example/tasktracker/entity/Task.java` mapped to `tasks` (id, title, description, status default PENDING, createdAt)
- [x] T006 Add `src/main/java/com/example/tasktracker/repository/TaskRepository.java` extending `JpaRepository<Task, Long>`
- [x] T007 [P] Add `src/main/java/com/example/tasktracker/exception/ErrorResponse.java` matching [contracts/openapi.yaml](./contracts/openapi.yaml) `ErrorResponse` shape (statusCode, message, errors[], timestamp, path)
- [x] T008 Add `src/main/java/com/example/tasktracker/exception/GlobalExceptionHandler.java` with `@RestControllerAdvice` handling `MethodArgumentNotValidException` → **400** + `ErrorResponse` field errors

**Checkpoint**: Flyway + entity + repository + validation error JSON shape ready.

---

## Phase 3: User Story 1 — Create a task with a title (Priority: P1) 🎯 MVP

**Goal**: **POST /v1/tasks** persists a task and returns **201** + body per [contracts/openapi.yaml](./contracts/openapi.yaml); invalid body returns **400** with field-level errors.

**Independent Test**: `curl` or tests: valid create returns id, title, optional description, `PENDING`, `createdAt`; blank title / overlong fields → **400** with `errors` array (see [quickstart.md](./quickstart.md)).

### Tests for User Story 1 (required — constitution TDD) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T009 [US1] Add failing `src/test/java/com/example/tasktracker/service/TaskServiceTest.java` — Mockito-based tests for create: happy path title-only, title + description, trim whitespace, reject blank title after trim, enforce max lengths (100 / 255)
- [x] T010 [US1] Add failing `src/test/java/com/example/tasktracker/controller/TaskControllerTest.java` — `MockMvc` tests for **POST** `/v1/tasks`: **201** JSON shape vs [contracts/openapi.yaml](./contracts/openapi.yaml); **400** validation for missing/blank title and oversized fields

### Implementation for User Story 1

- [x] T011 [P] [US1] Add `src/main/java/com/example/tasktracker/dto/request/CreateTaskRequest.java` with Jakarta validation (`@NotBlank`, `@Size`) for title and description per [data-model.md](./data-model.md)
- [x] T012 [P] [US1] Add `src/main/java/com/example/tasktracker/dto/response/TaskResponse.java` with fields id, title, description, status, createdAt (Jackson names match OpenAPI camelCase)
- [x] T013 [US1] Implement `src/main/java/com/example/tasktracker/service/TaskService.java` — create method: trim inputs, map DTO → entity, set `PENDING`, save via `TaskRepository`, map entity → `TaskResponse` (no entity leakage)
- [x] T014 [US1] Implement `src/main/java/com/example/tasktracker/controller/TaskController.java` — `@RestController` `@RequestMapping("/v1/tasks")` **POST** accepting `@Valid @RequestBody CreateTaskRequest`, returns `ResponseEntity.status(201).body(...)` calling `TaskService`

**Checkpoint**: `./mvnw test` green; US1 independently demonstrable via [quickstart.md](./quickstart.md).

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Verification and contract alignment.

- [x] T015 Run `./mvnw test` from repository root and fix any failures; ensure behavior matches `specs/001-tm-1-create-task/contracts/openapi.yaml` for **createTask**
- [x] T016 [P] Manual check: run app and execute curl examples in `specs/001-tm-1-create-task/quickstart.md` (201 + 400 paths)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → **Phase 2** → **Phase 3 (tests before implementation)** → **Phase 4**
- **T009–T010** MUST fail before **T011–T014** are completed.
- **T013** depends on **T005**, **T006**, **T011**, **T012**; **T014** depends on **T013** and **T008**

### User Story Dependencies

- **US1** only; no cross-story ordering.

### Parallel Opportunities

- **T007** and **T005** can proceed in parallel after **T004** (different files) if **T007** does not import domain types — prefer completing **T004** first, then **T005**/**T007** [P] where safe
- **T011** and **T012** parallel
- **T016** parallel with documentation-only follow-ups

---

## Parallel Example: User Story 1

```bash
# After Phase 2, write failing tests (can be parallelized by two developers):
Task: "TaskServiceTest.java — service create scenarios"
Task: "TaskControllerTest.java — MockMvc POST /v1/tasks"

# DTOs in parallel before service wiring:
Task: "CreateTaskRequest.java"
Task: "TaskResponse.java"
```

---

## Implementation Strategy

### MVP (User Story 1 only)

1. Complete Phase 1 and Phase 2  
2. **T009–T010** (red)  
3. **T011–T014** (green)  
4. **T015–T016** (verify)  
5. Stop — TM-1 scope complete; list/get/complete tasks are out of scope per [spec.md](./spec.md)

### Task summary

| Phase        | Task IDs | Count |
| ------------ | -------- | ----- |
| Setup        | T001–T003 | 3 |
| Foundational | T004–T008 | 5 |
| US1 tests    | T009–T010 | 2 |
| US1 impl     | T011–T014 | 4 |
| Polish       | T015–T016 | 2 |
| **Total**    |          | **16** |

---

## Notes

- Primary contract: `specs/001-tm-1-create-task/contracts/openapi.yaml`  
- Constitution: `.specify/memory/constitution.md` (layering, DTOs, Flyway, TDD)
