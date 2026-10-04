---
name: defining-done
description: Use before opening an MR or closing a ticket in Vedu, to confirm what "done" means for this kind of work — "is it done?", "MR ready?", "can I close the ticket?".
---

# Definition of Done (Vedu)

Walk Universal + the ONE matching section. Close only if every applicable box ticks and you ran the commands this session.

## Universal
- [ ] Minimal change; no unrelated refactors.
- [ ] Inside product boundary (CLAUDE.md §1); traces to a use case in PROJECT.md §4.1 or an SR-*.
- [ ] TDD followed: failing test seen first (CLAUDE.md §4).
- [ ] `cd backend && ./gradlew test` green for touched module(s); `./gradlew build` passes.
- [ ] No secrets/`.env` in diff; new config keys added to `.env.example`.
- [ ] Branch `feat|fix|chore|docs/…`, Conventional Commits with `#NN`.
- [ ] MR has `Closes #NN`, assignee self; ticket label `In Work`.
- [ ] AI_USAGE.md entry proposed if substantive (`ai-usage-report`).

## Backend feature
- [ ] `domain/**` imports no Spring/JPA/Redis/AMQP/Jackson; `application/**` no framework types in signatures.
- [ ] Business rule in domain/service, controller thin.
- [ ] Authorization order enforced server-side: authenticated → member → role → data → state; actor from session.
- [ ] Happy path + primary failure test per behavior; security behavior has a negative test named with its SR id.
- [ ] New port has a narrow interface; adapter lives in `infrastructure/`.
- [ ] Errors mapped in the module's `@RestControllerAdvice`; no stack traces/internal ids leaked.

## Status transition / role change
- [ ] Matrix test (role × from × to) updated first and passes; PROJECT.md §4.1.2 still matches code.
- [ ] Rework return requires a reason, stored with author.
- [ ] Stale-state action rejected, state unchanged.

## DB migration (Flyway)
- [ ] New versioned file (suffix `.pgsql` per `application.yaml`), never edit an applied one.
- [ ] Entity ↔ schema agree (`ddl-auto: validate` boots).
- [ ] Destructive change staged (add → backfill → switch → drop).

## Deploy / config
- [ ] `docker compose config` valid; secrets only to services that need them (worker gets Redis vars only).
- [ ] `.env.example` updated; nothing real committed.

## Closing the ticket
- [ ] MR merged → close ticket with comment (MR link) via `issue-note.py`.
- [ ] Linked epic: run `epic-status.py`.
- [ ] Follow-ups filed as separate tickets.

If a box can't be met: say so in the MR with the reason. If doing it means an epic-sized rewrite: stop and ask.
