---
description: Create or update a feature spec; optional Jira MCP; optional Roam Code MCP for repo context when configured.
handoffs: 
  - label: Build Technical Plan
    agent: sp.plan
    prompt: Create a plan for the spec. I am building with...
  - label: Clarify Spec Requirements
    agent: sp.clarify
    prompt: Clarify specification requirements
    send: true
---

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

**Examples**

```text
/sp.specify --name list-restaurants --jira TST-11
/sp.specify --name user-auth --ticket PROJ-123 "Extra context for the spec body"
/sp.specify --name analytics-dashboard --number 3 --jira KAN-42
/sp.specify "Plain description only" --name my-feature
/sp.specify --name list-restaurants --jira TST-11 --no-jira-status
```

- First three: **create** branch `NNN-<slug>` and spec from Jira via MCP (and **transition Jira to In Progress** unless `--no-jira-status`).  
- `Plain description` line: create branch from free text only (no Jira transition).  
- `--no-jira-status` line: fetch Jira for spec content but **do not** change Jira status.  
- Omit `--name` only when already on a **numbered** feature branch (e.g. `001-my-feature`) to **update** `specs/.../spec.md` in place.

## Optional: Roam Code (MCP)

[Roam Code](https://github.com/Cranot/roam-code) indexes the repo and exposes MCP tools (e.g. `roam_understand`, `roam_map`, `roam_endpoints`) for structure-aware context.

**When:** Use only if Roam MCP is enabled in Cursor (e.g. `roam-code` server; `roam_*` tools visible). If absent or a tool errors, **skip** — **never** block `/sp.specify`.

**Suggested uses (non-blocking):**

- **`roam_understand`** / **`roam_map`** — Stack, entry points, modules → inform **Assumptions** / **Out of scope** (keep user stories business-facing).
- **`roam_endpoints`** — Existing HTTP routes so the spec does not contradict the real API surface.

Do **not** require Roam for spec quality gates.

**Disclosure (audit):** In your final summary (and PHR `RESPONSE_TEXT` when applicable), include **one** line starting with `Roam MCP:` — e.g. `Roam MCP: used — roam_map` or `Roam MCP: skipped — not available` / `not needed` / `error (non-blocking)`.

## Outline

### Parse `$ARGUMENTS` (flags first)

Strip and interpret these **optional** flags from the user input (order-agnostic among flags; remaining text is the free-form description):

| Flag | Meaning |
|------|---------|
| `--name <kebab-slug>` | **Short name for the feature** (e.g. `list-restaurants`, `user-auth`). When this flag is present, you **MUST** run the feature-branch creation flow (see step 2 below) and pass `--short-name` to `create-new-feature.sh`. |
| `--jira <KEY>` or `--ticket <KEY>` | **Explicit Jira issue key** (e.g. `TST-11`, `PROJ-123`). You **MUST** load the story via **Atlassian Jira MCP** (see below). Do not rely on regex alone when this flag is set. |
| `--number <N>` | Optional override for feature branch number passed to `create-new-feature.sh --number N` (only valid with `--name`). |
| `--no-jira-status` | When set, **do not** change Jira workflow status (skip In Progress transition). |

**Aliases:** `-n` may be treated as `--name`; `-j` as `--jira`.

After removing flags, any remaining tokens form the **optional** free-text description (may be empty if Jira alone supplies content).

---

### Jira / ticket source of truth (MCP)

1. **If `--jira` / `--ticket` is present:**  
   - Call **Atlassian MCP** tool `getJiraIssue` with `issueIdOrKey` set to the key (e.g. `TST-11`).  
   - If your environment requires `cloudId`, resolve it using the Atlassian MCP site/cloud discovery flow for the user’s Jira site.  
   - From the response, assemble the feature description: **summary/title**, **description**, **acceptance criteria** (and any custom fields your template needs).  
   - Treat this as **source of truth** for the spec.  
   - Append a traceability line in the spec front matter, e.g. **Jira**: `[KEY](issue URL)` if the API returns a URL.

2. **Else if the remaining text matches a Jira key pattern** (e.g. `^[A-Z][A-Z0-9]+-\d+$` as the whole argument, or “Jira TST-11”):  
   - Optionally treat it as a key and call `getJiraIssue` the same way (same fields as above).

3. **Linear issues:** If the user passes a Linear-style id (e.g. `ANU-5`) or flag `--linear <ID>`, use **Linear MCP** `get_issue` with `id` and merge title/description into the feature description (same spec structure).

4. **If no Jira/Linear fetch occurred:** Use the remaining free text as the feature description.

5. **If, after all of the above, there is still no usable feature description:**  
   - ERROR: `No feature description provided. Pass text, --jira KEY, or --linear ID.`

---

### Jira: transition issue to **In Progress** (Atlassian MCP)

When a **Jira issue key** was used (`--jira` / `--ticket`, or heuristic fetch in step 2 above) **and** `--no-jira-status` was **not** passed:

1. **Resolve `cloudId`** (same value used for `getJiraIssue`): use **Atlassian MCP** `getAccessibleAtlassianResources` or the cloud/site id from the earlier issue response, per your environment.

2. **List allowed transitions** for that issue:  
   - Call **`getTransitionsForJiraIssue`** with `cloudId` and `issueIdOrKey` (the Jira key, e.g. `TST-11`).

3. **Pick the transition** whose **name** or **to-status** best matches **In Progress** for this project’s workflow:  
   - Prefer an exact case-insensitive match on names like `In Progress`, `In Development`, or `Doing` (use the **transition `id`** returned by the API).  
   - If the issue is **already** in a status that you treat as in-progress (e.g. current status name matches), **skip** the transition.

4. **Apply the transition**:  
   - Call **`transitionJiraIssue`** with:
     - `cloudId`
     - `issueIdOrKey`
     - `transition`: `{ "id": "<transition-id-from-step-3>" }`  
   - (Optional `fields` / `update` only if the workflow requires them—follow API errors if the transition fails.)

5. **Failure handling** (non-blocking for `/sp.specify`):  
   - If no suitable transition exists, the user lacks permission, or the API returns an error: **log a clear WARNING** in the completion report (include issue key and error summary) and **continue** creating/updating the spec.  
   - Do **not** abort the specify workflow solely because the transition failed.

**Linear:** Linear MCP does not use this Jira transition block; if you add Linear status updates later, use Linear’s issue-update tools separately.

---

### Feature branch creation (**only when `--name` is provided**)

When **`--name <kebab-slug>`** is present:

1. **Use the provided slug** as the short name (normalize to kebab-case if needed). Do **not** replace it with an auto-generated name unless the user omitted `--name`.

2. **Check for existing branches before creating a new one:**

   a. Fetch remotes:

      ```bash
      git fetch --all --prune
      ```

   b. Find the highest feature number `N` for this **exact** short-name across:
      - Remote branches: `git ls-remote --heads origin | grep -E 'refs/heads/[0-9]+-<short-name>$'`
      - Local branches: `git branch | grep -E '^[* ]*[0-9]+-<short-name>$'`
      - Specs directories: `specs/[0-9]+-<short-name>/`

   c. Next number is `N+1` (or use `--number` from flags if the user passed it).

3. Run `create-new-feature.sh` (once per feature):

   ```bash
   .specify/scripts/bash/create-new-feature.sh --json --number <N> --short-name "<kebab-slug>" "<feature description for commit message / metadata>"
   ```

   Use the Jira-fetched or user description as the **trailing** argument (not the raw `$ARGUMENTS` with flags). Parse JSON for `BRANCH_NAME`, `SPEC_FILE`, `FEATURE_NUM`.

   **IMPORTANT**:
   - Only match branches/directories with the **exact** short-name pattern.
   - If none exist for that short-name, start with number **1**.
   - Do not duplicate `--json` in the command line.
   - For single quotes in the description, use shell-safe quoting (see examples in older sections).

When **`--name` is omitted:**

- **Do not** run `create-new-feature.sh`.  
- Resolve the feature directory from the **current** context: run `.specify/scripts/bash/check-prerequisites.sh --json` (or equivalent `get_feature_paths`) and use `FEATURE_DIR` / `FEATURE_SPEC` only if the repo is already on a **numbered feature branch** (e.g. `001-my-feature`) or `SPECIFY_FEATURE` points at a valid `specs/NNN-name` directory.  
- If the current branch is **not** a valid feature branch (e.g. `main`, `feature/TST-11` without a matching `specs/` folder):  
  - ERROR: `Not on a numbered feature branch. Run /sp.specify with --name <kebab-slug> to create one, or checkout 001-<short-name> first.`

---

### After branch / paths are resolved

You now have:

- **Feature description** (from Jira MCP, Linear MCP, or free text — resolved in **Jira / ticket source of truth** above).
- **`FEATURE_DIR` / `SPEC_FILE`**: from `create-new-feature.sh` JSON when `--name` was used, or from `check-prerequisites.sh --json` when updating in place.

3. Load `.specify/templates/spec-template.md` to understand required sections.

4. Follow this execution flow:

    1. **Use the resolved feature description** (already built in **Parse `$ARGUMENTS`** / **Jira / ticket source of truth**). Do not re-fetch Jira unless the earlier step failed and you are recovering.
      - If description is still empty: ERROR `No feature description provided`
    2. Enrich feature description

      - If Jira/Linear story lacks sufficient detail:
         - Expand into structured format:
           - Problem statement
           - User stories
           - Functional requirements
           - Edge cases
           - Acceptance criteria

      - Ensure alignment with:
         - `.specify/memory/constitution.md`
         - `.specify/design-inputs/data-models/schema.sql`
         - `.specify/design-inputs/contracts/openapi.yaml`
    3. Extract key concepts from description
       Identify: actors, actions, data, constraints
    4. For unclear aspects:
       - Make informed guesses based on context and industry standards
       - Only mark with [NEEDS CLARIFICATION: specific question] if:
         - The choice significantly impacts feature scope or user experience
         - Multiple reasonable interpretations exist with different implications
         - No reasonable default exists
       - **LIMIT: Maximum 3 [NEEDS CLARIFICATION] markers total**
       - Prioritize clarifications by impact: scope > security/privacy > user experience > technical details
    5. Fill User Scenarios & Testing section
       If no clear user flow: ERROR "Cannot determine user scenarios"
    6. Generate Functional Requirements
       Each requirement must be testable
       Use reasonable defaults for unspecified details (document assumptions in Assumptions section)
    7. Define Success Criteria
       Create measurable, technology-agnostic outcomes
       Include both quantitative metrics (time, performance, volume) and qualitative measures (user satisfaction, task completion)
       Each criterion must be verifiable without implementation details
    8. Identify Key Entities (if data involved)
    9. Return: SUCCESS (spec ready for planning)

5. Write the specification to SPEC_FILE using the template structure, replacing placeholders with concrete details derived from the feature description (arguments) while preserving section order and headings.

6. **Specification Quality Validation**: After writing the initial spec, validate it against quality criteria:

   a. **Create Spec Quality Checklist**: Generate a checklist file at `FEATURE_DIR/checklists/requirements.md` using the checklist template structure with these validation items:

      ```markdown
      # Specification Quality Checklist: [FEATURE NAME]
      
      **Purpose**: Validate specification completeness and quality before proceeding to planning
      **Created**: [DATE]
      **Feature**: [Link to spec.md]
      
      ## Content Quality
      
      - [ ] No implementation details (languages, frameworks, APIs)
      - [ ] Focused on user value and business needs
      - [ ] Written for non-technical stakeholders
      - [ ] All mandatory sections completed
      
      ## Requirement Completeness
      
      - [ ] No [NEEDS CLARIFICATION] markers remain
      - [ ] Requirements are testable and unambiguous
      - [ ] Success criteria are measurable
      - [ ] Success criteria are technology-agnostic (no implementation details)
      - [ ] All acceptance scenarios are defined
      - [ ] Edge cases are identified
      - [ ] Scope is clearly bounded
      - [ ] Dependencies and assumptions identified
      
      ## Feature Readiness
      
      - [ ] All functional requirements have clear acceptance criteria
      - [ ] User scenarios cover primary flows
      - [ ] Feature meets measurable outcomes defined in Success Criteria
      - [ ] No implementation details leak into specification
      
      ## Notes
      
      - Items marked incomplete require spec updates before `/sp.clarify` or `/sp.plan`
      ```

   b. **Run Validation Check**: Review the spec against each checklist item:
      - For each item, determine if it passes or fails
      - Document specific issues found (quote relevant spec sections)

   c. **Handle Validation Results**:

      - **If all items pass**: Mark checklist complete and proceed to step 6

      - **If items fail (excluding [NEEDS CLARIFICATION])**:
        1. List the failing items and specific issues
        2. Update the spec to address each issue
        3. Re-run validation until all items pass (max 3 iterations)
        4. If still failing after 3 iterations, document remaining issues in checklist notes and warn user

      - **If [NEEDS CLARIFICATION] markers remain**:
        1. Extract all [NEEDS CLARIFICATION: ...] markers from the spec
        2. **LIMIT CHECK**: If more than 3 markers exist, keep only the 3 most critical (by scope/security/UX impact) and make informed guesses for the rest
        3. For each clarification needed (max 3), present options to user in this format:

           ```markdown
           ## Question [N]: [Topic]
           
           **Context**: [Quote relevant spec section]
           
           **What we need to know**: [Specific question from NEEDS CLARIFICATION marker]
           
           **Suggested Answers**:
           
           | Option | Answer | Implications |
           |--------|--------|--------------|
           | A      | [First suggested answer] | [What this means for the feature] |
           | B      | [Second suggested answer] | [What this means for the feature] |
           | C      | [Third suggested answer] | [What this means for the feature] |
           | Custom | Provide your own answer | [Explain how to provide custom input] |
           
           **Your choice**: _[Wait for user response]_
           ```

        4. **CRITICAL - Table Formatting**: Ensure markdown tables are properly formatted:
           - Use consistent spacing with pipes aligned
           - Each cell should have spaces around content: `| Content |` not `|Content|`
           - Header separator must have at least 3 dashes: `|--------|`
           - Test that the table renders correctly in markdown preview
        5. Number questions sequentially (Q1, Q2, Q3 - max 3 total)
        6. Present all questions together before waiting for responses
        7. Wait for user to respond with their choices for all questions (e.g., "Q1: A, Q2: Custom - [details], Q3: B")
        8. Update the spec by replacing each [NEEDS CLARIFICATION] marker with the user's selected or provided answer
        9. Re-run validation after all clarifications are resolved

   d. **Update Checklist**: After each validation iteration, update the checklist file with current pass/fail status

7. Report completion with branch name, spec file path, checklist results, readiness for the next phase (`/sp.clarify` or `/sp.plan`), and **Jira workflow** outcome when applicable: transitioned to In Progress (transition id/name), skipped (already in progress or `--no-jira-status`), or **warning** if transition failed.

**NOTE:** `create-new-feature.sh` runs **only when `--name` is provided**; it creates/checks out the numbered branch and spec directory. Without `--name`, the spec is written under the **current** numbered feature branch / `FEATURE_DIR` from prerequisites.

## General Guidelines

## Quick Guidelines

- Focus on **WHAT** users need and **WHY**.
- Avoid HOW to implement (no tech stack, APIs, code structure).
- Written for business stakeholders, not developers.
- DO NOT create any checklists that are embedded in the spec. That will be a separate command.

### Section Requirements

- **Mandatory sections**: Must be completed for every feature
- **Optional sections**: Include only when relevant to the feature
- When a section doesn't apply, remove it entirely (don't leave as "N/A")

### For AI Generation

When creating this spec from a user prompt:

1. **Make informed guesses**: Use context, industry standards, and common patterns to fill gaps
2. **Document assumptions**: Record reasonable defaults in the Assumptions section
3. **Limit clarifications**: Maximum 3 [NEEDS CLARIFICATION] markers - use only for critical decisions that:
   - Significantly impact feature scope or user experience
   - Have multiple reasonable interpretations with different implications
   - Lack any reasonable default
4. **Prioritize clarifications**: scope > security/privacy > user experience > technical details
5. **Think like a tester**: Every vague requirement should fail the "testable and unambiguous" checklist item
6. **Common areas needing clarification** (only if no reasonable default exists):
   - Feature scope and boundaries (include/exclude specific use cases)
   - User types and permissions (if multiple conflicting interpretations possible)
   - Security/compliance requirements (when legally/financially significant)

**Examples of reasonable defaults** (don't ask about these):

- Data retention: Industry-standard practices for the domain
- Performance targets: Standard web/mobile app expectations unless specified
- Error handling: User-friendly messages with appropriate fallbacks
- Authentication method: Standard session-based or OAuth2 for web apps
- Integration patterns: RESTful APIs unless specified otherwise

### Success Criteria Guidelines

Success criteria must be:

1. **Measurable**: Include specific metrics (time, percentage, count, rate)
2. **Technology-agnostic**: No mention of frameworks, languages, databases, or tools
3. **User-focused**: Describe outcomes from user/business perspective, not system internals
4. **Verifiable**: Can be tested/validated without knowing implementation details

**Good examples**:

- "Users can complete checkout in under 3 minutes"
- "System supports 10,000 concurrent users"
- "95% of searches return results in under 1 second"
- "Task completion rate improves by 40%"

**Bad examples** (implementation-focused):

- "API response time is under 200ms" (too technical, use "Users see results instantly")
- "Database can handle 1000 TPS" (implementation detail, use user-facing metric)
- "React components render efficiently" (framework-specific)
- "Redis cache hit rate above 80%" (technology-specific)

---

## SDD execution timing (Option A — `SDD-TIMELINE.md`)

Track wall-clock progress **without scripts**: maintain **`FEATURE_DIR/SDD-TIMELINE.md`** (columns: **Phase \| Started \| Completed**).

1. Resolve **`FEATURE_DIR`** (absolute `specs/NNN-feature` for this feature).
2. If **`SDD-TIMELINE.md`** does not exist there, create it from **`.specify/templates/sdd-timeline-template.md`** (preserve the header + table header row). If the file is legacy (**Phase \| Completed** only), add a **Started** column and use `—` for **Started** on existing rows.
3. **Start timestamp:** Right after steps 1–2, before substantive `/sp.specify` work, run `date -u +"%Y-%m-%dT%H:%M:%SZ"` → `start_ts`.
4. Perform the main `/sp.specify` work.
5. **Completed timestamp:** When that work finishes, run `date -u +"%Y-%m-%dT%H:%M:%SZ"` → `completed_ts`.
6. **Append** one row: `| specify | <start_ts> | <completed_ts> |` (each `/sp.specify` run adds a row; re-runs are allowed).
7. In your **final reply**, paste the **full table** under **### SDD execution time**.

Non-blocking if the file step is skipped.

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
