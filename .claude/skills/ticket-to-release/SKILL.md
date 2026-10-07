---
name: ticket-to-release
description: Use when asked to solve/take/fix a GitLab ticket by number (#NN) in Vedu — from reading the ticket to a merged MR. Not for code questions that need no changes.
---

# Ticket → merged MR

## 1. Read and bound
- `glab issue view <iid>` (or `glab api projects/:id/issues/<iid>`). Check against CLAUDE.md §1 and `docs/USE_CASES.md` §4.1. Out of boundary or ambiguous → ask, stop.
- Epic child? Read the epic body for principles and dependencies.
- Non-trivial → short plan first (what, why, how tested).

## 2. Isolate
- One ticket = one branch `feat|fix/<area>/<slug>` from fresh `main`. For parallel tickets use a subagent with `isolation: "worktree"`; give it: read CLAUDE.md first, ticket number + summary (not just the number), branch name, `Closes #NN`, TDD requirement (see failing test first), name the skills to use.
- Board: label → `In Work`, assignee self.

## 3. Implement with TDD
`superpowers:test-driven-development`. Layer order: domain test → domain → service test (mock ports) → service → adapter/controller slice test → adapter. Run `./gradlew :api:test --tests …` per step.

## 4. Gate
Walk `defining-done`. Run `cd backend && ./gradlew test build` (widen only as needed). Read the output; report counts honestly. Run `reviewing-ticket-work` before treating as done when available.

## 5. MR
Push branch, `glab mr create` with `Closes #NN`, assignee self, body = what/why/how tested/exceptions. Never merge red. Merge when pipeline passes, delete source branch.

## 6. Close the loop
Close ticket with comment (MR link) via `.claude/tools/issue-note.py`; `epic-status.py` if an epic child; file side findings as new tickets; propose an AI_USAGE.md entry.
