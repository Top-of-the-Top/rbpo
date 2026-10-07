---
name: structuring-epics
description: Use when creating or updating an epic issue in the Vedu repo, or naming/writing a child ticket that belongs to one — naming convention and required epic body sections.
---

# Structuring Epics

No native Epic work items on this GitLab tier. An epic is a plain Issue titled `[Epic] <name>` with labels `Epic` + one per other axis (see `creating-gitlab-issue`). Children attach via `relates_to` — see `linking-epic-children`. List epics: `glab issue list --label Epic`.

## Naming
- Epic: `[Epic] <name>`.
- Child: `<epic name>: <ticket name>` — keeps children grep-able and grouped.

## Epic body (headings English, prose Russian)
```markdown
### Description
What the epic is, one paragraph. Which use cases (UC-NN, `docs/USE_CASES.md` §4.1) and SR-* it covers.

### Why / principles
Why it matters + non-negotiable principles every child follows (CLAUDE.md §1 boundary, §3, §4 TDD).

### Plan
Numbered, in execution order. Dependencies inline (`— depends on #NN`), no separate section.

### Open questions
Unresolved forks. When closed: ~~strike through~~, mark **resolved**, one-line pointer to the ticket comment holding the reasoning (REQUIRED SUB-SKILL: `recording-decisions`).
```

## Rules
- Every ticket carries checkable acceptance criteria, not prose intent.
- Detail level: auth-style epics → detailed children; small teammate work → high-level children.
- Epic must fit the product boundary (CLAUDE.md §1). Outside it → ask the user.
- Keep the Plan current as children close; truth for closure is `epic-status.py`, not the checklist.

## Mistakes
- Child without the `<epic name>: ` prefix.
- Decision rationale pasted into "Open questions" instead of a pointer.
- Child created but not linked.
