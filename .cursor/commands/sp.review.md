---
description: Run CodeRabbit CLI + optional Semgrep MCP on code; append findings as CR### / SG### tasks in tasks.md (opt out with report-only / no-semgrep).
handoffs:
  - label: Implement tasks
    agent: sp.implement
    prompt: Implement review tasks — CodeRabbit (CR###), Semgrep (SG###), and remaining T### tasks
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

## Roam Code (MCP)

When **`roam_*` tools** are available, you **MUST** call **`roam_map`** or **`roam_endpoints`** once before scoping the review so CodeRabbit/Semgrep focus matches real modules and routes. If unavailable, proceed. **Disclosure:** one line `Roam MCP: …` in the final summary (and PHR when applicable).

- **Append tasks (default)**: After review(s), add each actionable finding as a task in `tasks.md` (see below). **Opt out** with: `report-only`, `no-tasks`, or `no-append` in `$ARGUMENTS`.
- **Semgrep**: **On by default** when the **Semgrep MCP** server is available (`semgrep` / `semgrep_scan`). **Opt out** with **`no-semgrep`** or **`semgrep-off`** in `$ARGUMENTS`.
- **Other overrides**: base branch (`--base develop`), output mode (`--plain`), fix mode (`fix` / `apply` to implement fixes after tasking). Use **`scan all`** in `$ARGUMENTS` to include non-code files (docs, scripts, config); otherwise scope is **code only** (default).

## Scope: code only (default)

**By default, only scan source code** so findings focus on application and test code, not docs or tooling.

- **Included (code)**: `src/` (all languages), and root build/config used by the app (e.g. `pom.xml`, `build.gradle`, `package.json`, `Cargo.toml`, `go.mod`). Include only if they are part of uncommitted changes.
- **Excluded (not code)**: `.cursor/`, `.specify/`, `specs/` (markdown/docs), `*.md`, `*.sh`, `*.yml` in `.github/` or `.specify/`, `history/`, `design-inputs/`, and similar doc/script/config that is not application source.
- **Override**: If the user says **`scan all`** (or equivalent) in `$ARGUMENTS`, do not restrict scope; include all findings.

**How to apply code-only:**

1. **When running the CLI**: If CodeRabbit supports path restriction (e.g. `--path src/` or `--include 'src/**'`), run with that so only code is submitted. Check `$CR_CLI --help` (after resolving `cr` or `coderabbit`) for options.
2. **When parsing findings**: Regardless of CLI, when in code-only mode, **only append tasks for findings whose file path is under code paths** (e.g. under `src/` or is a root build file). Ignore findings for excluded paths; do not add them to `tasks.md`.

## Goal

1. Run [CodeRabbit](https://docs.coderabbit.ai/cli) on **uncommitted changes** (or as configured).
2. When Semgrep MCP is enabled and not opted out, run **Semgrep** on the same **code scope** (local scan via MCP).
3. **Record all actionable findings as spec-kit tasks** (`CR###` = CodeRabbit, `SG###` = Semgrep) so they can be tracked and implemented (e.g. `/sp.implement`).

## Prerequisites

- **Git repository**: Run from repo root.
- **CodeRabbit CLI**:
  ```bash
  curl -fsSL https://cli.coderabbit.ai/install.sh | sh
  # or: brew install --cask coderabbit   # typically installs `coderabbit` (not always `cr`)
  ```
- **Auth** (once): `cr auth login` or `coderabbit auth login --api-key "cr-************"`

### CodeRabbit CLI binary (`cr` vs `coderabbit`)

Homebrew Cask and some installs expose **`coderabbit`** only; the docs often use **`cr`**. The agent **MUST** resolve the executable once and reuse it for all invocations:

1. If `command -v cr` succeeds → use **`cr`**.
2. Else if `command -v coderabbit` succeeds → use **`coderabbit`**.
3. Else → CLI missing; stop with install steps (see **Error handling**).

In shell examples below, **`$CR_CLI`** means this resolved binary (e.g. `export CR_CLI=cr` or `export CR_CLI=coderabbit`). Do not assume `cr` exists.

## Optional: Semgrep MCP

When the **Semgrep** MCP server is available in Cursor (e.g. **`plugin-semgrep-plugin-semgrep`** / tools `semgrep_scan`, `semgrep_findings`, `semgrep_scan_supply_chain`):

| Tool | Use in `/sp.review` |
|------|---------------------|
| **`semgrep_scan`** | **Primary.** Local scan; pass **`code_files`** as an array of `{ "path": "<absolute path>" }` for each file to scan. |
| **`semgrep_findings`** | **Optional second source.** Queries **Semgrep AppSec Platform** (already-uploaded findings). Use only if the project is connected and the user cares about cloud findings; pass `repos` (e.g. `owner/repo`) when known. **Do not** treat API errors as fatal — fall back to `semgrep_scan` only. |
| **`semgrep_scan_supply_chain`** | **Optional.** Supply-chain / dependency scan when lockfiles or manifests changed (e.g. `pom.xml`, `package.json`); add findings as **`SG###`** tasks same as SAST. |

**If Semgrep MCP is missing**, `no-semgrep` was passed, or all scan calls fail: **skip Semgrep** and continue with CodeRabbit only (non-blocking).

**File list for `semgrep_scan`:**

1. Collect **absolute paths** of files to scan:
   - **Default (code-only)**: `git diff --name-only HEAD` and `git diff --cached --name-only`; include only paths under **`src/`** (or your app’s code roots) and existing files.
   - If **no uncommitted code files** match, optionally scan **all** files under `src/` recursively (still respect code-only unless `scan all`).
2. **Batching**: If the list is large (>30 files), run **multiple** `semgrep_scan` calls in chunks (e.g. 20–30 files) and **merge** `results` for task generation.
3. Exclude the same non-code paths as in **Scope: code only** unless `scan all`.

## Outline

1. **Resolve feature paths** (same as other spec-kit commands):
   - From repo root, run `.specify/scripts/bash/check-prerequisites.sh --json` once and parse JSON for `FEATURE_DIR`, `FEATURE_SPEC`, `TASKS` (or derive `TASKS` = `FEATURE_DIR/tasks.md`).
   - If JSON fails, abort and instruct user to run `/sp.specify` or work from repo root on a feature branch.
   - For single quotes in args like "I'm Groot", use escape syntax: e.g. `'I'\''m Groot'` or double-quote: `"I'm Groot"`.

2. **Check environment**: Verify Git; resolve **`$CR_CLI`** per **CodeRabbit CLI binary** above (`cr` first, then `coderabbit`). If neither exists, report install steps and stop.

3. **Run CodeRabbit** (always via **`$CR_CLI`**):
   - **Default (agent-friendly)**: `$CR_CLI --prompt-only` from repo root.
   - **Plain text** (easier to parse or read): `$CR_CLI --plain` — use if `--prompt-only` output is too sparse or user asked for plain in `$ARGUMENTS`.
   - **Base branch** if not `main`: `$CR_CLI --prompt-only --base <branch>` per `$ARGUMENTS` or `git symbolic-ref` / user default.
   - **Explicit uncommitted**: `$CR_CLI --prompt-only --type uncommitted`
   - **Code only (default)**: If the CLI supports restricting paths (e.g. `--path src/` or `--include`), pass it so only code under `src/` (and optional root build files) is reviewed. If the CLI has no path option, run as usual and apply code-only filtering in step 6 (Parse findings). Use full scan only when `$ARGUMENTS` contains `scan all`.

4. **Run Semgrep (MCP)** — unless `$ARGUMENTS` contains **`no-semgrep`** / **`semgrep-off`**, or Semgrep tools are not listed in the MCP catalog:
   - Build the **`code_files`** list per **Optional: Semgrep MCP** above.
   - Invoke **`semgrep_scan`** (check MCP tool schema; required shape is `[{ "path": "<absolute>" }, ...]`).
   - Optionally invoke **`semgrep_findings`** for AppSec issues (merge with local scan; dedupe by file + rule + line).
   - Optionally invoke **`semgrep_scan_supply_chain`** when dependency manifests changed.
   - On partial failure, keep successful batches and **warn**; do not abort CodeRabbit tasking.

5. **Execution**:
   - Small diffs: foreground; large reviews / many Semgrep files: background with user notice (7–30+ min possible).

6. **Parse findings**:
   - **CodeRabbit**: Extract discrete, actionable items: severity (critical/high/medium/low if present), file path, line or region, short title, suggested fix or summary.
   - **Semgrep**: For each entry in `results` (and AppSec findings if used), map fields such as **`check_id` / rule id**, **`path`**, **`start` / `end` line**, **`extra.message`** (or equivalent in the MCP JSON) into the same shape: severity, path, location, title, fix hint.
   - **Code-only filter (default)**: Unless `$ARGUMENTS` contains `scan all`, **include only findings for code paths**: files under `src/` or root build files (`pom.xml`, `build.gradle`, `package.json`, etc.). Discard findings for `.cursor/`, `.specify/`, `specs/*.md`, `*.sh`, `.github/*.yml`, `history/`, and other excluded paths; do not add them as tasks.
   - **Dedupe across tools**: If CodeRabbit and Semgrep report the **same file + same line + same class of issue**, prefer **one task** (keep `CR###` or `SG###` with a note “also flagged by Semgrep/CodeRabbit”).
   - If **both** return no actionable items after filtering, report success and **do not** add empty sections.

7. **Append tasks to `tasks.md`** (unless `$ARGUMENTS` contains `report-only`, `no-tasks`, or `no-append`):

   - **If `TASKS` file is missing**: create `FEATURE_DIR/tasks.md` with a minimal header **or** write `FEATURE_DIR/review-findings-YYYYMMDD.md` with the same task list and tell the user to merge into `tasks.md` when it exists. Prefer creating/append to `tasks.md` only if `plan.md` or `spec.md` exists in `FEATURE_DIR` (feature is initialized).

   - **Task ID schemes**:
     - **`CR###`** — CodeRabbit. Scan `TASKS` for the highest existing `CR` id; continue from next integer.
     - **`SG###`** — Semgrep (local MCP scan and/or AppSec/supply-chain). Scan `TASKS` for the highest existing `SG` id; continue from next integer. **Do not** reuse `CR###` for Semgrep.

   - **CodeRabbit task line format**:

     ```markdown
     - [ ] CR### [CodeRabbit] [<SEVERITY>] <path>:<location> — <concise title>. Fix: <one-line action or reference to finding>
     ```

     Example:

     ```markdown
     - [ ] CR007 [CodeRabbit] [HIGH] src/main/java/com/example/orders/service/OrderService.java:142 — Possible NPE when lineItems null. Fix: add guard or Optional as suggested in review
     ```

   - **Semgrep task line format**:

     ```markdown
     - [ ] SG### [Semgrep] [<SEVERITY>] <path>:<line> — <rule or check id>: <short message>. Fix: <one-line remediation or doc link if present>
     ```

     Example:

     ```markdown
     - [ ] SG003 [Semgrep] [MEDIUM] src/main/java/com/example/demo/controller/Foo.java:22 — java.lang.security.rule: Use parameterized query. Fix: replace string concat SQL with PreparedStatement
     ```

   - **Sections to append** (after existing content; add only sections that have ≥1 task):

     ```markdown
     ---

     ## Phase: CodeRabbit review findings

     **Purpose**: Remediate CodeRabbit CLI findings from `/sp.review`. **Generated**: YYYY-MM-DD (ISO). Re-run after fixes; check off when done.

     - [ ] CR### ...
     ```

     ```markdown
     ---

     ## Phase: Semgrep review findings

     **Purpose**: Remediate Semgrep (MCP / AppSec) findings from `/sp.review`. **Generated**: YYYY-MM-DD (ISO). Re-run after fixes; check off when done.

     - [ ] SG### ...
     ```

   - If a phase section **already exists** for **today’s date**, **append** new lines under it (no duplicate header) or merge deduplicated findings.

8. **Evaluate and fix (optional)**:
   - If user said `fix` or `apply` in `$ARGUMENTS`: implement critical/high items (or all, per user); still **append tasks first** for traceability unless user said `fix without tasks`.
   - Otherwise: summarize counts by severity **per source** (CodeRabbit vs Semgrep), list new **`CR###`** and **`SG###`** ids, and point to `TASKS` path.

9. **Optional second pass**: After fixes, re-run CodeRabbit and/or Semgrep MCP; append only **new** findings as new ids (never reuse `CR###` / `SG###`).

## Command examples (for the agent)

```bash
# Resolve once (cr preferred; Homebrew Cask often installs coderabbit only):
CR_CLI="$(command -v cr 2>/dev/null || command -v coderabbit 2>/dev/null)"
"$CR_CLI" --prompt-only
"$CR_CLI" --plain
"$CR_CLI" --prompt-only --base develop
"$CR_CLI" --prompt-only --type uncommitted
# Code only (use if CLI supports it; otherwise filter in parse step):
"$CR_CLI" --prompt-only --path src/
```

## Integration with spec kit

- **After /sp.implement**: `/sp.review` → tasks for CodeRabbit + Semgrep → `/sp.implement` for `CR###` / `SG###` / `T###`.
- **Before /sp.git.commit_pr**: Run `/sp.review`, clear or complete critical **`CR###` / `SG###`** tasks as appropriate, then commit.

## Error handling

- **Not a Git repo**: "CodeRabbit requires a Git repository. Run from repo root."
- **CodeRabbit CLI not found** (`cr` and `coderabbit` both missing): Install instructions; stop.
- **Auth**: `$CR_CLI auth login` (or `cr auth login` / `coderabbit auth login`); stop.
- **Rate limit**: Note limits (e.g. Free: 3/hour); suggest retry later.
- **`tasks.md` read-only or missing parent dir**: Write `review-findings-YYYYMMDD.md` under `FEATURE_DIR` and report path.
- **Semgrep MCP errors**: Warn; continue with CodeRabbit-only tasks if applicable.

## Output

- Path to updated `tasks.md` (or fallback file) and counts of **`CR###`** and **`SG###`** tasks added.
- Summary table: severity breakdown **by source** (CodeRabbit vs Semgrep); list of new task IDs.
- One-line audit: **`Semgrep MCP: used — semgrep_scan`** (and other tools if called) **or** **`Semgrep MCP: skipped — not available | no-semgrep | error (non-blocking)`**.
- If **report-only**: full summary only; no file writes.

---

As the main request completes, you MAY create a PHR for this run.

- **Stage**: `misc` or `general` (use `misc` + `--feature` if tied to active feature).
- **Title**: e.g. "CodeRabbit and Semgrep findings as tasks"
- On PHR failure: warn, don't block.
