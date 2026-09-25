---
name: creating-gitlab-issue
description: Use when filing a new GitLab issue in the Vedu (rbpo) repo — reporting a bug, proposing a feature, logging an analysis task, or the user says "create an issue", "open a ticket", "заведи тикет", "создай issue".
---

# Creating a GitLab Issue (Vedu)

## Overview

Every issue has an English title, English section headings, a **Russian** body, exactly one label per axis, and a **mandatory assignee**. The assignee is what triggers the Telegram notification (GitLab issue webhook → relay → group chat), so an unassigned issue notifies nobody. Confirm title, body, labels and assignee with the user before creating: issues are visible, shared state.

Check the ticket against the product boundary in CLAUDE.md and the use cases in PROJECT.md §4.1 first; a ticket for something outside the boundary is a question for the user, not an issue.

## Steps

1. Draft the body with the Write tool into a scratch file (UTF-8, structure below).
2. Show the user title, labels, assignee and body; wait for approval.
3. Validate: `python .claude/tools/issue-create.py --title "…" --body-file body.md --label Backlog,Medium,Backend,Feature --assignee <username> --dry-run`
4. Re-run without `--dry-run`. Report the `iid` and URL it prints.

`--assignee` defaults to the authenticated `glab` user; pass it explicitly when the ticket is for someone else. The tool exits non-zero if a label is unknown, an axis has zero or two labels, the assignee does not exist, or GitLab did not apply what was asked. Do not work around a failure with raw `glab issue create`.

## Labels — one per axis

| Axis | Values | Default |
|---|---|---|
| State | `Plans`, `Backlog`, `In Work` | `Backlog` |
| Severity | `Major` (blocks the lifecycle or security), `Medium`, `Low` | ask |
| Area | `Backend`, `Deploy`, `Docs` | ask |
| Type | `Bug`, `Feature`, `Analysis`, `Epic` | ask |

Do not invent labels: GitLab silently creates any unknown label. To add an axis value, ask the user, create it in GitLab, then add it to `AXES` in `issue-create.py`. Re-check the list with `glab api "projects/:id/labels?per_page=100"` if the tool rejects a label you expect to exist.

## Body structure

```markdown
### Problem
Что не так или что нужно, по-русски, с причиной. Для бага: файл:строка и как воспроизвести.

### Impact
Почему это важно и что будет, если не делать.

### Solution
Что предлагается сделать, шагами.

### Acceptance criteria
- [ ] Проверяемый результат (конкретное поведение или проверка)

### Notes
Связанные тикеты (#NN), use case из PROJECT.md §4.1, ограничения.
```

Acceptance criteria are checkable statements, not "works correctly". Title: a short English imperative or noun phrase, no label prefixes.

## Common mistakes

- Creating without an assignee, or with a display name instead of the GitLab username.
- Two labels on one axis, or a label from outside the four axes.
- Russian headings or an English body — headings English, prose Russian.
- Creating before the user approved the draft.
- Filing a ticket for a feature outside the product boundary.
