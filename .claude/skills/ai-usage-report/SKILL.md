---
name: ai-usage-report
description: Reports substantive AI usage to AI_USAGE.md per assignment requirements. Use when the user says "report AI usage", "log AI usage", "add to AI_USAGE", "we used AI for this", or after a change that was influenced by AI (requirements, solution, code behavior, tests, analysis).
---

You maintain AI_USAGE.md for the Vedu project. Your job is to append or update an entry when there was substantive AI use, following the assignment rules exactly.

## What counts as substantive AI use

Log it if AI influenced:
- the meaning of requirements or documents
- the chosen solution or approach
- code behavior
- test content
- analysis conclusions

Do NOT log: typos, formatting, single-term translation, short autocomplete, reference questions with no substantive impact.

## Required fields per entry

Every entry must contain:

- **Участник**: `M3`, `A100`, or `E95` (the team's actual codes, from `PROJECT.md`; ask the user if not provided)
- **Задача**: what problem was being solved
- **Использование ИИ**: how AI was used
- **В проект вошло**: what actually went into the project (be precise — "переработанная версия" not just "сгенерировано")
- **Проверка человеком**: what the human actually verified (only what was really done — do not invent tests)
- **Связанный результат**: file, SR-*, T-*, D-*, commit, PR, or test

Do NOT include full names, group numbers, or personal emails. Use the `M3`/`A100`/`E95` codes only.

## Infrastructure section stays separate

`AI_USAGE.md` has a one-time "Инфраструктура ИИ" section (tool, `CLAUDE.md` context,
skills, hooks) that is declared once, not per task — do not duplicate it or fold it
into a task entry. Only touch it if the actual tooling changes (new skill added/removed,
hook changed); otherwise leave it as is.

## How to write

1. Read the current AI_USAGE.md first.
2. If it still has the short declaration ("Существенное использование... отсутствует"), replace it with the first entry, but keep the "Инфраструктура ИИ" section if present.
3. If related entries exist for the same task, update them rather than adding duplicates.
4. Keep entries concise. No need for full prompts, responses, or screenshots.

## Workflow

1. Ask the user which participant (`M3`/`A100`/`E95`) is responsible.
2. Ask what task was solved and how AI was used.
3. Confirm what actually went into the project.
4. Confirm what human verification was actually performed.
5. Get the link to the related result (commit, file, SR-*).
6. Append or update the entry in AI_USAGE.md.

If any of these are unclear, ask before writing. Never invent verification.