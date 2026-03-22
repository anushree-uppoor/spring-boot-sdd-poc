---
id: "0001"
title: "Specify TM-1 create task branch"
stage: "spec"
date: "2026-03-22T17:38:38Z"
surface: "cursor-chat"
model: "unknown"
feature: "001-tm-1-create-task"
branch: "001-tm-1-create-task"
user: "unknown"
command: "/sp.specify"
labels: [spec, jira-tm-1, task-tracker]
links:
  spec: "specs/001-tm-1-create-task/spec.md"
  ticket: "TM-1"
  adr: "n/a"
  pr: "n/a"
files:
  - "specs/001-tm-1-create-task/spec.md"
  - "specs/001-tm-1-create-task/checklists/requirements.md"
  - "specs/001-tm-1-create-task/SDD-TIMELINE.md"
tests:
  - "none (spec authoring only)"
---

## Prompt

/sp.specify --name TM-1-Create-Task --jira TM-1

## Response snapshot

Created branch **001-tm-1-create-task**, wrote **spec.md** (P1 create-task story, FRs, SCs, assumptions), **checklists/requirements.md** (all items checked after validation), **SDD-TIMELINE.md** (specify row). Jira **getJiraIssue** / transition not executed — Atlassian MCP tools not available; traceability notes TM-1 and design inputs. Roam MCP skipped (tool not available).

## Outcome

- ✅ Impact: Feature spec ready for `/sp.clarify` or `/sp.plan`.
- 🧪 Tests: Not run.
- 📁 Files: spec + checklist + timeline + PHR.
- 🔁 Next prompts: `/sp.plan` (or `/sp.clarify`).
- 🧠 Reflection: When Jira MCP works, re-sync summary/description from TM-1 into spec front matter.

## Evaluation notes (flywheel)

- Failure modes observed: Jira fetch blocked (no MCP tool).
- Graders run and results (PASS/FAIL): n/a
- Prompt variant (if applicable): n/a
- Next experiment (smallest change to try): Authenticate Atlassian MCP and re-run specify update for TM-1 body only.
