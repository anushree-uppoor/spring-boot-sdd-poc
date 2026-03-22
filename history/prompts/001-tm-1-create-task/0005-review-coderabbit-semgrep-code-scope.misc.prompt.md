---
id: "0005"
title: "Review CodeRabbit Semgrep code scope"
stage: "misc"
date: "2026-03-22T18:12:00Z"
surface: "cursor-chat"
model: "unknown"
feature: "001-tm-1-create-task"
branch: "001-tm-1-create-task"
user: "unknown"
command: "/sp.review"
labels: [review, coderabbit, semgrep]
links:
  spec: "specs/001-tm-1-create-task/spec.md"
  ticket: "n/a"
  adr: "n/a"
  pr: "n/a"
files:
  - "specs/001-tm-1-create-task/tasks.md (unchanged — no code-scoped tasks)"
tests:
  - "n/a"
---

## Prompt

/sp.review 

## Response snapshot

**CodeRabbit:** `coderabbit review --prompt-only --type uncommitted --plain` — 3 findings, all under `specs/.../openapi.yaml`; dropped under default **code-only** scope (`src/` + root build). **Semgrep MCP:** `semgrep_scan` on 13 files (`pom.xml` + Java under `src/`) — **0 results**. **tasks.md:** no new **CR###** / **SG###** (per “no empty sections” when nothing qualifies). Roam skipped.

## Outcome

- ✅ Impact: Review run recorded; no code debt tasks from filtered tools.
- 🧪 Tests: n/a
- 📁 Files: none modified
- 🔁 Next prompts: Re-run `/sp.review scan all` to task OpenAPI suggestions; or `/sp.git.commit_pr`.
- 🧠 Reflection: CodeRabbit binary at `/opt/homebrew/bin/coderabbit`; shell `cr` was an alias string, not a path.

## Evaluation notes (flywheel)

- Failure modes observed: none
- Graders run and results (PASS/FAIL): n/a
- Prompt variant (if applicable): n/a
- Next experiment (smallest change to try): `scan all` once to capture contract CR tasks if desired.
