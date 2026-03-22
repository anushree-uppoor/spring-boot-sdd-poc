# Data Model: TM-1 Create Task

## Entity: Task

| Field        | Type        | Constraints | Notes |
| ------------ | ----------- | ----------- | ----- |
| id           | BIGINT      | PK, auto-generated | Exposed as integer/long in API |
| title        | VARCHAR(100)| NOT NULL    | Trimmed; non-blank after trim |
| description  | VARCHAR(255)| NULL        | Optional; trimmed if present |
| status       | VARCHAR(20) | NOT NULL, default `PENDING` | CHECK: `PENDING` \| `COMPLETED` |
| created_at   | TIMESTAMP   | NOT NULL, default now | Maps to API `createdAt` |

## Validation rules (API / domain)

- **title**: required; max length **100**; after trim, must not be blank.
- **description**: optional; max length **255**; if present, trim edges.

## State transitions

- On **create** only for TM-1: status is always **`PENDING`**. (Completed transition is out of scope.)

## Relationships

- None for TM-1 (standalone `tasks` table).

## Flyway

- Single migration **V1__create_tasks_table.sql** (or next available version if repo already has migrations) creating `tasks` with columns above and indexes on `status` and `created_at` per [schema.sql](../../.specify/design-inputs/data-models/schema.sql).
