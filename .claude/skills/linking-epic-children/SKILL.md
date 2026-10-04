---
name: linking-epic-children
description: Use when a child ticket must be attached to an epic in the Vedu repo, or when checking whether all of an epic's children are closed (epic closure condition).
---

# Linking Epic Children

`relates_to` links substitute for parent/child (see `structuring-epics`).

## Link (when the child is created, not batched later)
```bash
python .claude/tools/epic-link.py <epic_iid> <child_iid> [<child_iid> ...]
```
Re-linking an existing pair fails with 409 — expected.

## Close an epic
```bash
python .claude/tools/epic-status.py <epic_iid>
```
Lists each linked child's real state; exit 0 = closeable (all closed), 1 = open children remain. Close the epic with `issue-note.py <epic_iid> --body-file … --close` and a summary comment.

## Mistakes
- Unlinked child (invisible from its own page).
- Trusting the epic's Plan checklist over `epic-status.py`.
