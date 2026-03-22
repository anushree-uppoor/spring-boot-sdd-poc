---
description: Run CodeRabbit CLI to review uncommitted code; append findings as tasks in tasks.md (opt out with report-only).
handoffs:
  - label: Implement tasks
    agent: sp.implement
    prompt: Implement CodeRabbit tasks (CR###) and remaining T### tasks
    send: false
  - label: Commit & open PR
    agent: sp.git.commit_pr
    prompt: Commit changes and open PR
    send: false
---

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

- **Append tasks (default)**: After review, add each actionable finding as a task in `tasks.md` (see below). **Opt out** with: `report-only`, `no-tasks`, or `no-append` in `$ARGUMENTS`.
- **Other overrides**: base branch (`--base develop`), output mode (`--plain`), fix mode (`fix` / `apply` to implement fixes after tasking). Use **`scan all`** in `$ARGUMENTS` to include non-code files (docs, scripts, config); otherwise scope is **code only** (default).

## Scope: code only (default)

**By default, only scan source code** so findings focus on application and test code, not docs or tooling.

- **Included (code)**: `src/` (all languages), and root build/config used by the app (e.g. `pom.xml`, `build.gradle`, `package.json`, `Cargo.toml`, `go.mod`). Include only if they are part of uncommitted changes.
- **Excluded (not code)**: `.cursor/`, `.specify/`, `specs/` (markdown/docs), `*.md`, `*.sh`, `*.yml` in `.github/` or `.specify/`, `history/`, `design-inputs/`, and similar doc/script/config that is not application source.
- **Override**: If the user says **`scan all`** (or equivalent) in `$ARGUMENTS`, do not restrict scope; include all findings.

**How to apply code-only:**

1. **When running the CLI**: If CodeRabbit supports path restriction (e.g. `--path src/` or `--include 'src/**'`), run with that so only code is submitted. Check `cr --help` / `coderabbit review --help` for options.
2. **When parsing findings**: Regardless of CLI, when in code-only mode, **only append tasks for findings whose file path is under code paths** (e.g. under `src/` or is a root build file). Ignore findings for excluded paths; do not add them to `tasks.md`.

## Goal

Run [CodeRabbit](https://docs.coderabbit.ai/cli) to review **uncommitted changes** in the current Git repo, then **record findings as spec-kit tasks** so they can be tracked and implemented (e.g. via `/sp.implement` or manually).

## Prerequisites

- **Git repository**: Run from repo root.
- **CodeRabbit CLI**:
  ```bash
  curl -fsSL https://cli.coderabbit.ai/install.sh | sh
  # or: brew install coderabbit
  ```
- **Auth** (once): `cr auth login` or `coderabbit auth login --api-key "cr-************"`

## Outline

1. **Resolve feature paths** (same as other spec-kit commands):
   - From repo root, run `.specify/scripts/bash/check-prerequisites.sh --json` once and parse JSON for `FEATURE_DIR`, `FEATURE_SPEC`, `TASKS` (or derive `TASKS` = `FEATURE_DIR/tasks.md`).
   - If JSON fails, abort and instruct user to run `/sp.specify` or work from repo root on a feature branch.
   - For single quotes in args like "I'm Groot", use escape syntax: e.g. `'I'\''m Groot'` or double-quote: `"I'm Groot"`.

2. **Check environment**: Verify Git; verify `cr` or `coderabbit` on PATH. If CLI missing, report install steps and stop.

3. **Run CodeRabbit**:
   - **Default (agent-friendly)**: `cr --prompt-only` from repo root.
   - **Plain text** (easier to parse or read): `cr --plain` — use if `--prompt-only` output is too sparse or user asked for plain in `$ARGUMENTS`.
   - **Base branch** if not `main`: `cr --prompt-only --base <branch>` per `$ARGUMENTS` or `git symbolic-ref` / user default.
   - **Explicit uncommitted**: `cr --prompt-only --type uncommitted`
   - **Code only (default)**: If the CLI supports restricting paths (e.g. `--path src/` or `--include`), pass it so only code under `src/` (and optional root build files) is reviewed. If the CLI has no path option, run as usual and apply code-only filtering in step 5 (Parse findings). Use full scan only when `$ARGUMENTS` contains `scan all`.

4. **Execution**:
   - Small diffs: foreground; large reviews: background with user notice (7–30+ min possible).

5. **Parse findings**:
   - Extract discrete, actionable items: severity (critical/high/medium/low if present), file path, line or region, short title, suggested fix or summary.
   - **Code-only filter (default)**: Unless `$ARGUMENTS` contains `scan all`, **include only findings for code paths**: files under `src/` or root build files (`pom.xml`, `build.gradle`, `package.json`, etc.). Discard findings for `.cursor/`, `.specify/`, `specs/*.md`, `*.sh`, `.github/*.yml`, `history/`, and other excluded paths; do not add them as tasks.
   - Skip duplicates (same file + same issue type).
   - If CodeRabbit returns no findings (or none pass the code-only filter), report success and **do not** add an empty section.

6. **Append tasks to `tasks.md`** (unless `$ARGUMENTS` contains `report-only`, `no-tasks`, or `no-append`):

   - **If `TASKS` file is missing**: create `FEATURE_DIR/tasks.md` with a minimal header **or** write `FEATURE_DIR/coderabbit-findings-YYYYMMDD.md` with the same task list and tell the user to merge into `tasks.md` when it exists. Prefer creating/append to `tasks.md` only if `plan.md` or `spec.md` exists in `FEATURE_DIR` (feature is initialized).

   - **Task ID scheme**: Use **`CR###`** (CodeRabbit) so they do not collide with existing **`T###`** tasks. Scan `TASKS` for the highest existing `CR` id (`CR001`, `CR002`, …) and continue numbering from the next integer.

   - **Task line format** (align with spec-kit checklist style):

     ```markdown
     - [ ] CR### [CodeRabbit] [<SEVERITY>] <path>:<location> — <concise title>. Fix: <one-line action or reference to finding>
     ```

     Example:

     ```markdown
     - [ ] CR007 [CodeRabbit] [HIGH] src/main/java/com/example/orders/service/OrderService.java:142 — Possible NPE when lineItems null. Fix: add guard or Optional as suggested in review
     ```

   - **Section to append** (once per run, after existing content):

     ```markdown
     ---

     ## Phase: CodeRabbit review findings

     **Purpose**: Remediate AI code review findings from CodeRabbit CLI (`/sp.coderabbit`). **Generated**: YYYY-MM-DD (ISO). Re-run `/sp.coderabbit` after fixes to refresh; remove or check off completed items.

     - [ ] CR### ...
     ```

   - If a **Phase: CodeRabbit review findings** section already exists for **today’s date**, either:
     - **Append** new `CR###` lines under that section (no duplicate section header), or
     - **Replace** same-day duplicate findings if the user re-ran review (merge by title+file deduplication).

7. **Evaluate and fix (optional)**:
   - If user said `fix` or `apply` in `$ARGUMENTS`: implement critical/high items (or all, per user); still **append tasks first** for traceability unless user said `fix without tasks`.
   - Otherwise: summarize counts by severity, list new `CR###` ids, and point to `TASKS` path.

8. **Optional second pass**: After fixes, run `cr --prompt-only` again; append any **new** findings as additional `CR###` tasks (never reuse ids).

## Command examples (for the agent)

```bash
cr --prompt-only
cr --plain
cr --prompt-only --base develop
cr --prompt-only --type uncommitted
# Code only (use if CLI supports it; otherwise filter in parse step):
cr --prompt-only --path src/
# or: coderabbit review --plain --path src/
```

## Integration with spec kit

- **After /sp.implement**: `/sp.coderabbit` → tasks for findings → `/sp.implement` to work `CR###` items.
- **Before /sp.git.commit_pr**: Run review, clear or complete critical `CR###` tasks, then commit.

## Error handling

- **Not a Git repo**: "CodeRabbit requires a Git repository. Run from repo root."
- **`cr` not found**: Install instructions; stop.
- **Auth**: `cr auth login`; stop.
- **Rate limit**: Note limits (e.g. Free: 3/hour); suggest retry later.
- **`tasks.md` read-only or missing parent dir**: Write `coderabbit-findings-YYYYMMDD.md` under `FEATURE_DIR` and report path.

## Output

- Path to updated `tasks.md` (or fallback file) and count of **`CR###`** tasks added.
- Summary table: severity breakdown; list of new task IDs.
- If **report-only**: full summary only; no file writes.

---

As the main request completes, you MAY create a PHR for this run.

- **Stage**: `misc` or `general` (use `misc` + `--feature` if tied to active feature).
- **Title**: e.g. "CodeRabbit findings as tasks"
- On PHR failure: warn, don't block.
