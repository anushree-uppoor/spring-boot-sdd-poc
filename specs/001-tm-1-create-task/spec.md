# Feature Specification: TM-1 Create Task

**Feature Branch**: `001-tm-1-create-task`  
**Created**: 2026-03-22  
**Status**: Draft  
**Input**: `/sp.specify --name TM-1-Create-Task --jira TM-1`  
**Traceability**: **Jira** TM-1 — ticket body was not retrieved here (Atlassian MCP tools unavailable); scope is aligned with the Task Tracker “create task” capability and shared design inputs (OpenAPI + logical data model).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a task with a title (Priority: P1)

A user adds a new task to their tracker by providing a short name for the work. They may also add extra notes. After saving, they see confirmation that the task exists and is in a “not yet completed” state so they can find it later.

**Why this priority**: Without create, no other task workflows have data to operate on; this is the smallest slice that delivers standalone value.

**Independent Test**: Submit a valid create with only a title (and optionally with a description), then confirm the response includes a unique identifier, the title (and description if supplied), an initial not-completed status, and a creation timestamp.

**Acceptance Scenarios**:

1. **Given** the user is ready to capture work, **When** they submit a create with a non-empty title and no description, **Then** the system accepts it and returns a confirmation with a new identifier and not-completed status.
2. **Given** the user has optional details, **When** they submit a create with a non-empty title and a description, **Then** the system stores both and returns them in the confirmation.
3. **Given** the user submits invalid input (missing or blank title), **When** the create is processed, **Then** the system rejects it with a clear, field-level validation message and does not create a task.

### Edge Cases

- Title exceeds the maximum length allowed for task names (reject with validation feedback).
- Description exceeds the maximum length allowed for notes (reject with validation feedback).
- Leading or trailing whitespace on title or description is normalized or rejected consistently (same rule every time).
- Very rapid repeated submissions: each valid request creates a distinct task (no silent overwrite unless product rules say otherwise — **assumption**: duplicates allowed).

## Requirements *(mandatory)*

**Constitution compliance:** Implementation MUST satisfy `.specify/memory/constitution.md` — layered **Controller → Service → Repository**, **DTO** boundaries (no entity exposure on APIs), **Flyway-only** schema changes, and **TDD** (failing tests before production code).

### Functional Requirements

- **FR-001**: Users MUST be able to create a task by supplying a non-empty title within the published length limit.
- **FR-002**: Users MUST be able to optionally supply a description within the published length limit.
- **FR-003**: On successful create, the system MUST return a stable identifier, the stored title and description (if any), an initial not-completed status, and when the task was created.
- **FR-004**: On invalid input, the system MUST reject the request without persisting a task and MUST indicate which fields failed validation.
- **FR-005**: Created tasks MUST be durable: after success, the same identifier MUST refer to the same task content for later retrieval (within normal product uptime).

### Key Entities *(include if feature involves data)*

- **Task**: A unit of work with a human-readable title, optional longer description, a completion status (initially not completed), a creation moment, and a system-assigned identifier.

### Assumptions

- Authentication and multi-tenant isolation are out of scope unless separately specified; a single trusted client is assumed for this slice.
- Field length limits match the shared Task Tracker design: title up to 100 characters, description up to 255 characters (logical model).
- Listing tasks, completing tasks, and editing tasks are out of scope for this feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In usability testing, at least **90%** of participants successfully create a valid task on the first try when given only the task name to enter.
- **SC-002**: For valid creates under normal conditions, users receive a success confirmation within **5 seconds** in **95%** of attempts (measured from submit to confirmed response).
- **SC-003**: **100%** of rejected creates include identifiable validation feedback tied to the offending field(s) (audit a sample of at least 20 invalid attempts).
- **SC-004**: After success, **100%** of sampled tasks can be retrieved by identifier in a follow-up lookup within the same environment (spot-check at least 10 creates).
