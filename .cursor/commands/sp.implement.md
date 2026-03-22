---
description: Execute tasks from tasks.md; run tests and report test summary + coverage; optional Roam Code MCP.
---

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Optional: Roam Code (MCP)

[Roam Code](https://github.com/Cranot/roam-code) supports **preflight**, **context**, and **diff** over the code graph.

**When:** Only if `roam_preflight`, `roam_prepare_change`, `roam_context`, `roam_diff` (or similar) are available. Otherwise **skip** — **never** block implementation or tests.

**Suggested uses (non-blocking, advisory only):**

- **`roam_preflight`** / **`roam_prepare_change`** before substantive edits — blast radius, tests, complexity.
- **`roam_context`** when tasks lack exact touch points.
- **`roam_diff`** after edits (optional) — structural impact vs `tasks.md`.

**Disclosure (audit):** One line `Roam MCP: …` in the final summary (and PHR when applicable); for long runs you may list all tools once at the end.

## Outline

1. Run `.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks` from repo root and parse FEATURE_DIR and AVAILABLE_DOCS list. All paths must be absolute. For single quotes in args like "I'm Groot", use escape syntax: e.g 'I'\''m Groot' (or double-quote if possible: "I'm Groot").

2. **Check checklists status** (if FEATURE_DIR/checklists/ exists):
   - Scan all checklist files in the checklists/ directory
   - For each checklist, count:
     - Total items: All lines matching `- [ ]` or `- [X]` or `- [x]`
     - Completed items: Lines matching `- [X]` or `- [x]`
     - Incomplete items: Lines matching `- [ ]`
   - Create a status table:

     ```text
     | Checklist | Total | Completed | Incomplete | Status |
     |-----------|-------|-----------|------------|--------|
     | ux.md     | 12    | 12        | 0          | ✓ PASS |
     | test.md   | 8     | 5         | 3          | ✗ FAIL |
     | security.md | 6   | 6         | 0          | ✓ PASS |
     ```

   - Calculate overall status:
     - **PASS**: All checklists have 0 incomplete items
     - **FAIL**: One or more checklists have incomplete items

   - **If any checklist is incomplete**:
     - Display the table with incomplete item counts
     - **STOP** and ask: "Some checklists are incomplete. Do you want to proceed with implementation anyway? (yes/no)"
     - Wait for user response before continuing
     - If user says "no" or "wait" or "stop", halt execution
     - If user says "yes" or "proceed" or "continue", proceed to step 3

   - **If all checklists are complete**:
     - Display the table showing all checklists passed
     - Automatically proceed to step 3

3. Load and analyze the implementation context:
   - **REQUIRED**: Read tasks.md for the complete task list and execution plan
   - **REQUIRED**: Read plan.md for tech stack, architecture, and file structure
   - **IF EXISTS**: Read data-model.md for entities and relationships
   - **IF EXISTS**: Read contracts/ for API specifications and test requirements
   - **IF EXISTS**: Read research.md for technical decisions and constraints
   - **IF EXISTS**: Read quickstart.md for integration scenarios

4. **Project Setup Verification**:
   - **REQUIRED**: Create/verify ignore files based on actual project setup:

   **Detection & Creation Logic**:
   - Check if the following command succeeds to determine if the repository is a git repo (create/verify .gitignore if so):

     ```sh
     git rev-parse --git-dir 2>/dev/null
     ```

   - Check if Dockerfile* exists or Docker in plan.md → create/verify .dockerignore
   - Check if .eslintrc* exists → create/verify .eslintignore
   - Check if eslint.config.* exists → ensure the config's `ignores` entries cover required patterns
   - Check if .prettierrc* exists → create/verify .prettierignore
   - Check if .npmrc or package.json exists → create/verify .npmignore (if publishing)
   - Check if terraform files (*.tf) exist → create/verify .terraformignore
   - Check if .helmignore needed (helm charts present) → create/verify .helmignore

   **If ignore file already exists**: Verify it contains essential patterns, append missing critical patterns only
   **If ignore file missing**: Create with full pattern set for detected technology

   **Common Patterns by Technology** (from plan.md tech stack):
   - **Node.js/JavaScript/TypeScript**: `node_modules/`, `dist/`, `build/`, `*.log`, `.env*`
   - **Python**: `__pycache__/`, `*.pyc`, `.venv/`, `venv/`, `dist/`, `*.egg-info/`
   - **Java**: `target/`, `*.class`, `*.jar`, `.gradle/`, `build/`
   - **C#/.NET**: `bin/`, `obj/`, `*.user`, `*.suo`, `packages/`
   - **Go**: `*.exe`, `*.test`, `vendor/`, `*.out`
   - **Ruby**: `.bundle/`, `log/`, `tmp/`, `*.gem`, `vendor/bundle/`
   - **PHP**: `vendor/`, `*.log`, `*.cache`, `*.env`
   - **Rust**: `target/`, `debug/`, `release/`, `*.rs.bk`, `*.rlib`, `*.prof*`, `.idea/`, `*.log`, `.env*`
   - **Kotlin**: `build/`, `out/`, `.gradle/`, `.idea/`, `*.class`, `*.jar`, `*.iml`, `*.log`, `.env*`
   - **C++**: `build/`, `bin/`, `obj/`, `out/`, `*.o`, `*.so`, `*.a`, `*.exe`, `*.dll`, `.idea/`, `*.log`, `.env*`
   - **C**: `build/`, `bin/`, `obj/`, `out/`, `*.o`, `*.a`, `*.so`, `*.exe`, `Makefile`, `config.log`, `.idea/`, `*.log`, `.env*`
   - **Swift**: `.build/`, `DerivedData/`, `*.swiftpm/`, `Packages/`
   - **R**: `.Rproj.user/`, `.Rhistory`, `.RData`, `.Ruserdata`, `*.Rproj`, `packrat/`, `renv/`
   - **Universal**: `.DS_Store`, `Thumbs.db`, `*.tmp`, `*.swp`, `.vscode/`, `.idea/`

   **Tool-Specific Patterns**:
   - **Docker**: `node_modules/`, `.git/`, `Dockerfile*`, `.dockerignore`, `*.log*`, `.env*`, `coverage/`
   - **ESLint**: `node_modules/`, `dist/`, `build/`, `coverage/`, `*.min.js`
   - **Prettier**: `node_modules/`, `dist/`, `build/`, `coverage/`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`
   - **Terraform**: `.terraform/`, `*.tfstate*`, `*.tfvars`, `.terraform.lock.hcl`
   - **Kubernetes/k8s**: `*.secret.yaml`, `secrets/`, `.kube/`, `kubeconfig*`, `*.key`, `*.crt`

5. Parse tasks.md structure and extract:
   - **Task phases**: Setup, Tests, Core, Integration, Polish
   - **Task dependencies**: Sequential vs parallel execution rules
   - **Task details**: ID, description, file paths, parallel markers [P]
   - **Execution flow**: Order and dependency requirements

6. Execute implementation following the task plan:
   - **Phase-by-phase execution**: Complete each phase before moving to the next
   - **Respect dependencies**: Run sequential tasks in order, parallel tasks [P] can run together  
   - **Follow TDD approach**: Execute test tasks before their corresponding implementation tasks
   - **File-based coordination**: Tasks affecting the same files must run sequentially
   - **Validation checkpoints**: Verify each phase completion before proceeding

7. Implementation execution rules:
   - **Setup first**: Initialize project structure, dependencies, configuration
   - **Tests before code**: If you need to write tests for contracts, entities, and integration scenarios
   - **Core development**: Implement models, services, CLI commands, endpoints
   - **Integration work**: Database connections, middleware, logging, external services
   - **Polish and validation**: Unit tests, performance optimization, documentation

8. Progress tracking and error handling:
   - Report progress after each completed task
   - Halt execution if any non-parallel task fails
   - For parallel tasks [P], continue with successful tasks, report failed ones
   - Provide clear error messages with context for debugging
   - Suggest next steps if implementation cannot proceed
   - **IMPORTANT** For completed tasks, make sure to mark the task off as [X] in the tasks file.

9. Completion validation:
   - Verify all required tasks are completed
   - Check that implemented features match the original specification
   - **Run the full test suite** and produce **Test summary** + **Coverage** per **Test verification and coverage reporting** below (required in the final reply)
   - Confirm the implementation follows the technical plan
   - Report final status with summary of completed work

Note: This command assumes a complete task breakdown exists in tasks.md. If tasks are incomplete or missing, suggest running `/sp.tasks` first to regenerate the task list.

## Test verification and coverage reporting

After implementation work, **before** the final user-facing summary, run tests from **repository root** and include structured results.

### 1. Detect toolchain (from `plan.md`, `pom.xml`, `package.json`, `build.gradle`, `pyproject.toml`, `go.mod`, etc.)

| Stack | Typical test command | Typical coverage |
|-------|----------------------|------------------|
| **Maven / Java** | `./mvnw test` or `mvn test` | JaCoCo: `./mvnw test jacoco:report` (if JaCoCo plugin present in `pom.xml`) |
| **Gradle / Java** | `./gradlew test` | `./gradlew test jacocoTestReport` (if configured) |
| **Node (npm/pnpm/yarn)** | `npm test` | `npm test -- --coverage` (Jest/Vitest) or tool in `package.json` |
| **Python** | `pytest` or `python -m pytest` | `pytest --cov=<pkg> --cov-report=term-missing` |
| **Go** | `go test ./...` | `go test -cover ./...` |
| **.NET** | `dotnet test` | `dotnet test /p:CollectCoverage=true` (Coverlet) if configured |

Use the commands that match the repo; if **multiple** stacks exist, run the **primary** one from `plan.md`.

### 2. Run tests (required)

- Execute the **full** test suite (not a single file), unless `tasks.md` explicitly scoped tests to specific modules.
- Capture **exit code** and **console output** (or Surefire/Gradle report paths).
- If tests **fail**: report failures first; do not claim implementation complete until failures are fixed **or** the user explicitly accepts leaving tests red (invoke human if policy unclear).

### 3. Test summary (required in final reply)

Include a subsection **`### Test summary`** with:

- **Command(s)** run
- **Outcome**: PASS / FAIL
- **Counts** when available: tests run, failures, errors, skipped (e.g. Maven Surefire, JUnit summary line, pytest last line)
- **Failed tests** (if any): class/method names + short error message

Example (shape, not literal):

```text
### Test summary

- Command: `./mvnw test`
- Outcome: PASS
- Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
- Time: 4.2 s
```

### 4. Coverage report (required when tooling exists)

- If the project **has** a coverage plugin or script (JaCoCo in `pom.xml`, Jest `--coverage`, pytest-cov, etc.), run it and add **`### Coverage`**:
  - **Overall** line or branch % if printed, **or** path to generated HTML report (e.g. `target/site/jacoco/index.html`) and one-sentence summary
  - If the tool writes **XML/JSON**, you may quote the top-level totals

- If coverage is **not** configured anywhere, write:

  ```text
  ### Coverage

  Not configured in this repository (no JaCoCo/Jest/pytest-cov/etc. detected). Optional follow-up: add JaCoCo (Maven) or equivalent per team standards.
  ```

Coverage collection is **non-blocking** for SDD completion if adding a plugin was out of scope—**still** run plain `test` and deliver **Test summary**.

### 5. PHR and checklists

- Copy a **short** test + coverage excerpt into PHR **`RESPONSE_TEXT`** / **TESTS_YAML** when applicable.
- If `plan.md` or constitution mandates a **minimum coverage %**, note pass/fail against that threshold in **Test summary** or **Coverage**.

---

## SDD execution timing (Option A — `SDD-TIMELINE.md`)

1. Resolve **`FEATURE_DIR`**. Ensure **`SDD-TIMELINE.md`** exists (create from **`.specify/templates/sdd-timeline-template.md`** if missing). If legacy two-column only, add **Started** and use `—` on existing rows.
2. **Start timestamp:** Before executing tasks / code edits for this run, `date -u +"%Y-%m-%dT%H:%M:%SZ"` → `start_ts`.
3. Perform the main `/sp.implement` work (including tests at the end of the flow).
4. **Completed timestamp:** After implementation + required test reporting for this command completes, `date -u +"%Y-%m-%dT%H:%M:%SZ"` → `completed_ts`.
5. **Append** `| implement | <start_ts> | <completed_ts> |` (each `/sp.implement` run adds a row; re-runs are allowed).
6. **Final reply:** include **`### Test summary`** and **`### Coverage`** (per **Test verification and coverage reporting** above), then paste the **full table** under **### SDD execution time**.

---

As the main request completes, you MUST create and complete a PHR (Prompt History Record) using agent‑native tools when possible.

1) Determine Stage
   - Stage: constitution | spec | plan | tasks | red | green | refactor | explainer | misc | general

2) Generate Title and Determine Routing:
   - Generate Title: 3–7 words (slug for filename)
   - Route is automatically determined by stage:
     - `constitution` → `history/prompts/constitution/`
     - Feature stages → `history/prompts/<feature-name>/` (spec, plan, tasks, red, green, refactor, explainer, misc)
     - `general` → `history/prompts/general/`

3) Create and Fill PHR (Shell first; fallback agent‑native)
   - Run: `.specify/scripts/bash/create-phr.sh --title "<title>" --stage <stage> [--feature <name>] --json`
   - Open the file and fill remaining placeholders (YAML + body), embedding full PROMPT_TEXT (verbatim) and concise RESPONSE_TEXT.
   - If the script fails:
     - Read `.specify/templates/phr-template.prompt.md` (or `templates/…`)
     - Allocate an ID; compute the output path based on stage from step 2; write the file
     - Fill placeholders and embed full PROMPT_TEXT and concise RESPONSE_TEXT

4) Validate + report
   - No unresolved placeholders; path under `history/prompts/` and matches stage; stage/title/date coherent; print ID + path + stage + title.
   - On failure: warn, don't block. Skip only for `/sp.phr`.
