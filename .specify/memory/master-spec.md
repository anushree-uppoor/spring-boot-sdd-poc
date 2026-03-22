# Master specification — Task Tracker (rolled up)

**Last synced:** 2026-03-22  
**Sources:** Active feature spec `specs/001-tm-1-create-task/spec.md`, implementation under `src/main/java/com/example/tasktracker/`, Flyway `V1__create_tasks_table.sql`.

**Traceability:** TM-1 (Create Task); Jira body not bound in-repo.

---

## Product intent

Small **Task Tracker** API: users capture work as **tasks** with a **title** and optional **description**, persisted with a **stable id**, **status** (not completed vs completed), and **creation time**. This POC uses **H2** in-memory and **Spring Boot 4** with **Controller → Service → Repository**, **DTOs** on the wire, and **Flyway** for schema.

---

## Implemented behavior (as of last sync)

### HTTP API

| Method | Path | Status | Behavior |
|--------|------|--------|----------|
| POST | `/v1/tasks` | **201** | Creates a task. Body: JSON `title` (required, max 100), `description` (optional, max 255). Response: `id`, `title`, `description` (nullable), `status` (`PENDING` on create), `createdAt` (ISO-8601). |
| — | — | **400** | Bean validation failures return structured JSON: `statusCode`, `message`, `errors[]` with `field` + `message`, `timestamp`, `path`. |

### Domain rules reflected in code

- **Title:** `@NotBlank` / `@Size(max=100)` at API boundary; **service** trims leading/trailing whitespace before persist.
- **Description:** optional `@Size(max=255)`; trim; blank-after-trim stored as **null**.
- **New tasks:** `status` is **PENDING**; `created_at` set on persist (entity `@PrePersist` + DB default).

### Persistence

- Table **`tasks`:** `id`, `title`, `description`, `status`, `created_at`; check constraint on `status` ∈ `PENDING`, `COMPLETED`.
- Schema owned by **Flyway** only (`ddl-auto: validate`).

---

## Specified but not implemented in this codebase yet

Per feature spec **out of scope** and broader Task Tracker contract (not present as live routes):

- List tasks (**GET** `/v1/tasks`).
- Get task by id (**GET** `/v1/tasks/{id}`).
- Mark complete (**PUT** `/v1/tasks/{id}/complete`).
- Auth / multi-tenant (explicitly out of scope for TM-1).

**Note:** Success criterion **SC-004** (“retrieve by identifier”) assumes a future **GET by id**; create path is implemented and durable in DB, but **no read endpoint** exists yet.

---

## Contract references

- Feature OpenAPI slice: `specs/001-tm-1-create-task/contracts/openapi.yaml`
- Constitution: `.specify/memory/constitution.md`

---

## How to refresh

Re-run **`/sp.sync-master-spec`** after spec or code changes (especially before **`/sp.git.commit_pr`**). Merge additional features by extending this document from their `specs/<branch>/spec.md` plus code.
