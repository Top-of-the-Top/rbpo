# CLAUDE.md — Vedu

## Project
Vedu — small-team task tracker. Author creates a task, assigns an executor,
then accepts the result or returns it for rework. Board with fixed columns;
status changes are drag-and-drop, but **the server validates every transition**.

## Product boundary (do not expand)
- Fixed statuses only. No custom columns, no arbitrary transition schemas.
- No sprints, epics, Gantt, time tracking, estimates, reports.
- In scope beyond the core lifecycle: subtasks, recurring tasks, List view alongside the Board (two views only),
  comments with @mentions, file attachments, real-time board updates, login + email + password with one-time code.
  The rework reason is a separate mandatory field, not a comment.
- Not in scope: custom fields, task templates, guest/external access, external integrations, OAuth.
- Use cases live in PROJECT.md §4.1 — check them before adding behavior.
- Roles only: author / executor / team member / team creator.

## Core rules
- The task lifecycle is the heart of the product. Status transition rules
  live on the server. The client never decides what is allowed.
- Every transition must be checked: who (role) → from which status → to which status.
- Return to rework always requires a reason.
- Auth is required for every action; there are no anonymous operations.
- Author and executor of one task are different users; only the author accepts or returns a task.
- Comments, attachments and real-time events are visible only to members of the task's team; check membership server-side.

## How to work
1. **Understand before changing.** Read the relevant files, ask if unclear.
2. **Plan first** for any non-trivial change: what changes, why, how it will be tested.
3. **One scenario at a time** (team formation → task execution → acceptance/rework).
4. **Never invent features** outside the product boundary. If unsure — ask.
5. **Do not move permission logic to the client.** Server is the source of truth.
6. **After any substantive change, propose what to log in AI_USAGE.md.**

## Rules for the agent
- Do not generate code you cannot explain line by line.
- Do not silently rewrite unrelated code.
- Do not add dependencies without asking.
- Do not touch secrets, credentials, personal data.
- Do not claim a test passed unless it was actually run.
- If the task is ambiguous, stop and ask rather than guess.

## What counts as substantive AI use (for AI_USAGE.md)
Log it if AI influenced: requirements, chosen solution, code behavior,
test content, or analysis conclusions.
Do NOT log: typos, formatting, single-term translation, short autocomplete.