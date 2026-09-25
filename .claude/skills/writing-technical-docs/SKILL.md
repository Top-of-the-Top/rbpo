---
name: writing-technical-docs
description: Use when writing or restructuring a technical document in this repo — threat model, requirements, design doc, analysis, README section — in markdown, especially one with IDs and cross-links to other documents.
---

# Writing Technical Docs

## Overview

A document contains facts the reader needs, in tables and diagrams, each fact in one place. Everything else is cut.

## Document shape

1. **Title.**
2. **1–3 lines**: what is inside, with anchor links to sections; legend only if the diagrams need it.
3. **Content sections**: diagram → table, or table alone. Headings name the subject ("B. Смена статуса задачи"), not the activity ("Анализ").
4. **Nothing else.** Notation, assumptions, introductions, "how to read this" and closing summaries are added only when the user asks for them.

## Rules for content

- **Facts only.** If something is undecided, either ask the user or pick a working choice and state it once in the reply to the user, not in the document.
- **Tables for anything enumerable**; each row stands alone (ID, subject, the fact, link).
- **One fact — one place.** If a column repeats what another document already holds (e.g. "проверка" = an SR acceptance criterion), reference that ID instead of copying.
- **Full traceability.** Every item links to its counterpart (threat → requirement, flow → diagram). No "gap", "TBD" or "—" left in a link column: close it by adding the missing item upstream, then link.
- **Stable IDs.** Renumber freely until the document is shared; after that, never.

## Before handing over

1. **Redundancy pass**: delete sections that restate a heading or table; merge rows describing the same thing; drop low-value items instead of keeping them as "accepted".
2. **Consistency check** with a short script, not by eye: every ID referenced exists, every link is bidirectional.
   ```bash
   grep -oE 'F[0-9]+' STRIDE.md | sort -u > used; grep -oE '^\| F[0-9]+' DFD.md | tr -d '| ' | sort -u > defined; comm -23 used defined
   ```
3. Diagrams inside follow **designing-diagrams**.

## Common Mistakes

| Symptom | Fix |
| --- | --- |
| Intro, notation, assumptions sections | Remove; title + links + content |
| "разрыв" / "TBD" in a link column | Add the missing requirement, then link |
| Same data in two documents | Keep in one, reference by ID |
| Hedged prose ("может быть", "по умолчанию") | State the chosen fact or ask |
| Summary table restating the main table | Remove |
