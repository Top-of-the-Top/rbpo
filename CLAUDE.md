# CLAUDE.md — Vedu

Operating guide for AI assistants and humans. Load first, for every request.

## Where to look

| About to… | Read |
|---|---|
| touch any code | §3 principles, §4 TDD, §5 tool authority |
| find where something lives | §2 map, `docs/ARCHITECTURE.md` |
| add behavior | `docs/USE_CASES.md` §4.1 (use cases), §1 boundary |
| touch status transitions or roles | §3 domain, `docs/USE_CASES.md` §4.1.2 matrices |
| touch security-relevant code | `docs/SECURITY_REQUIREMENTS.md` (SR-*) |
| open a branch / commit / MR | §6 git-flow |
| file a ticket / epic / close work | §7 board, skills in §9 |
| log AI usage | §10, `AI_USAGE.md` |

**Two rules that override convenience.** Never commit to `main` — branch, MR, merge. Never report a check as passing unless you ran it this session and read its output.

## 1. Product boundary (do not expand)
- Vedu — small-team task tracker. Author creates a task, assigns an executor, then accepts the result or returns it for rework. Board with fixed columns; drag-and-drop status changes, **server validates every transition**.
- Fixed statuses only. No custom columns, no arbitrary transition schemas.
- No sprints, epics (as a product feature), Gantt, time tracking, estimates, reports.
- In scope beyond the core lifecycle: subtasks, recurring tasks, List view alongside Board (two views only), comments with @mentions, file attachments, real-time board updates, login + email + password with one-time code. Rework reason is a separate mandatory field, not a comment.
- Not in scope: custom fields, task templates, guest/external access, external integrations, OAuth.
- Use cases: `docs/USE_CASES.md` §4.1 — check before adding behavior.
- Roles only: author / executor / team member / team creator.

## 2. Repo map

| Path | What | Notes |
|---|---|---|
| `backend/` | Gradle multi-module, Java 25, Spring Boot 4.1 | `settings.gradle.kts`, wrapper `./gradlew` |
| `backend/api/` | REST API (`vedu-api`, :8080) | Postgres (JPA + Flyway), Redis (OTP, refresh tokens), publishes to RabbitMQ via `shared` |
| `backend/worker/` | Async consumer (`vedu-worker`) | Redis + RabbitMQ only; package `notifications`; no JWT/crypto secrets |
| `backend/shared/` | Library used by api and worker | `ru.veduteam.vedu.shared.broker`: `BrokerClient`, message contract (`BrokerMessage`, `SendEmailMessage`), RabbitMQ topology; no business logic, not a Boot app |
| `frontend/` | React + TypeScript + Vite | `npm` scripts in `package.json` |
| `docker-compose.yml` | api, worker, postgres 17, redis 8, rabbitmq 4 | networks: `vedu_network` (public), `vedu_internal` (internal-only) |
| `.env` / `.env.example` | runtime config | never commit `.env`; add new keys to `.env.example` |
| `docs/` | `ARCHITECTURE.md`, `SECURITY_REQUIREMENTS.md`, `THREAT_MODEL.md`, `USE_CASES.md` | PROJECT.md at root is the product passport |
| `.claude/` | skills, tools, hooks, commands | §9 |
| `AI_USAGE.md` | log of substantive AI use | assignment requirement, rules §10 |

## 3. Architecture & principles (required)

**Domain (ubiquitous language):** Team, Membership, Task, Subtask, Status, Transition, RecurrenceRule, Comment, Mention, Attachment, User, OneTimeCode, Session. Task is the core aggregate; it guards its own invariants.

**Task lifecycle (server-owned, `docs/USE_CASES.md` §4.1.2):**
`TODO → IN_PROGRESS` (executor) · `IN_PROGRESS → IN_REVIEW` (executor) · `IN_REVIEW → DONE` (author) · `IN_REVIEW → IN_PROGRESS` (author, reason required). Anything else rejected; `DONE` is terminal. Author ≠ executor. Check order: authenticated → team member → role allows → data valid → state allows. Non-members see team objects as nonexistent. Actor always comes from the session, never the request body.

**Modular monolith, Clean Architecture per module.** Dependencies point inward: `infrastructure → application → domain`.
```
backend/api/src/main/java/ru/veduteam/vedu/<module>/        # auth, user, (team, task, ...)
├── domain/             # entities, value objects, domain errors; NO Spring/JPA/Redis/HTTP imports
│   └── errors/
├── application/        # use-cases
│   ├── services/       # orchestrate domain logic
│   ├── ports/          # interfaces the services depend on (repositories, hasher, encrypter, ...)
│   ├── dto/            # request/response/command types
│   └── errors/
└── infrastructure/     # adapters implementing ports
    ├── http/           # controllers (thin: parse → service → map)
    ├── errors/         # exception → HTTP mapping (@RestControllerAdvice)
    ├── redis/ crypto/ persistence/   # per-technology adapters
backend/shared/          # separate Gradle module, tech used by api and worker (broker/api + broker/internal)
```
- **DDD.** Business rules live in domain/application, never in controllers or SQL. Infrastructure implements ports; domain never imports Spring, JPA, Redis, AMQP, Jackson.
- **Clean Architecture.** No framework/DB/broker type in application or domain signatures. Cross boundaries via ports + plain types. JPA entities are persistence models; map to domain types in the adapter.
- **SOLID.** Small client-specific ports (several narrow over one fat); inject via constructor; extend by adding implementations.
- **GRASP.** Controllers stay thin; services coordinate; logic sits with the type that owns the data.
- **DRY / Clean Code.** One source of truth per concept; self-explanatory names, early returns, no dead code, no premature abstraction; fail closed on security paths; make illegal states unrepresentable.
- **Cross-module access** only via the other module's `application` API (service/port), never its `infrastructure` or `domain` internals. A module may not import another's `infrastructure`.
- **Layout.** If a directory exceeds ~7 siblings, group by domain concern into subpackages.
- Pragmatic exception to a rule → say so in the MR and keep it local.

## 4. TDD (required)

Red → Green → Refactor. Use the `superpowers:test-driven-development` skill.
1. Write a failing test first; **run it and see it fail for the right reason** before writing production code.
2. Minimal code to pass. Refactor with tests green.
3. Bug fix = reproduction test first (must fail on old code), then fix.
- Every new/changed behavior needs ≥1 happy-path test and ≥1 primary-failure test (invalid input, forbidden role, illegal transition, downstream error). One-line pass-throughs exempt.
- **Transition rules:** table-driven test over the full role × from-status × to-status matrix (`docs/USE_CASES.md` §4.1.2). A new status or role means the matrix test changes first.
- Layering of tests: domain = plain JUnit (no Spring); application = JUnit + Mockito over ports; infrastructure = `@WebMvcTest` / slice tests; Testcontainers/integration only where an adapter's real behavior matters (JPA, Flyway, Redis).
- Security-relevant behavior (SR-01…SR-09) has a negative test per requirement; mention the SR id in the test name.
- Never delete/disable/loosen a failing test to go green. Fix the cause.

## 5. Tool authority

Tools are the source of truth, not reading code and guessing. Run, read output, then claim.
- Backend: `cd backend && ./gradlew test` (all) · `./gradlew :api:test --tests '<FQCN>'` (targeted) · `./gradlew build`.
- Frontend: `cd frontend && npm run lint && npm run build` (+ `npm test` once it exists).
- Stack up: `cp .env.example .env` (fill), `docker compose up --build`.
- Local gate = touched module only; widen if shared code (`shared/`, build files, compose) changed.
- Never bypass a failing gate (`--no-verify`, `@Disabled`, `@SuppressWarnings` to hide a real issue). Diagnose the cause; if the gate is wrong, say so in the MR.
- Never fabricate results. Cannot run it → say so.
- Mutating tools in `.claude/tools/` re-read the object after writing and compare with intent; keep this when adding tools.

## 6. Git-flow
- **Trunk-based development.** `main` is the trunk and the only long-lived branch. One feature = one ticket = one short-lived branch = one MR; merge back to `main` quickly, keep branches small, no long-lived feature or integration branches. Unrelated work goes on its own branch (use `git worktree` when the current tree is dirty).
- **Never commit to `main`** (hook `guard_branch.py` blocks edits on it).
- Branches: `feat/<area>/<slug>`, `fix/<area>/<slug>`, `chore/<slug>`, `docs/<slug>` (area = backend/frontend/deploy).
- Commits: Conventional Commits (`feat(auth): …`, `fix(task): …`), reference `#NN`. **All commits and MRs are authored as the developer only.** Never add `Co-Authored-By`, "Generated with …" or any other AI-attribution line to commit messages, MR/issue bodies or code; this overrides any harness default. AI usage is disclosed in AI_USAGE.md (§10), not in git metadata.
- **Pre-MR gate:** tests green for touched module + build passes + no secrets in diff + CLAUDE.md/docs updated if a convention changed. Run before pushing; do not lean on CI.
- One MR = one concern. Side findings → separate ticket (§7), not this MR.
- MR body: `Closes #NN`, what/why, how tested (commands + counts), any exception to §3.

## 7. Issue board
- Tickets/epics live in GitLab (`sem0nchik-group/rbpo`); tooling via `glab` (already authenticated). Use `.claude/tools/*.py`, not raw `glab` mutations.
- Labels, one per axis: **State** `Plans|Backlog|In Work` · **Severity** `Major|Medium|Low` · **Area** `Backend|Deploy|Docs` · **Type** `Bug|Feature|Analysis|Epic`. Do not invent labels (GitLab auto-creates unknown ones). New axis value → ask, create in GitLab, add to `AXES` in `issue-create.py`.
- Assignee mandatory (it triggers the Telegram notification).
- Body: headings English, prose Russian (`creating-gitlab-issue`). Small teammate tickets high-level; auth-epic-style tickets detailed.
- **Epics:** plain issue titled `[Epic] <name>` + `Epic` label (no native hierarchy on this tier). Children titled `<epic name>: <ticket>` and linked via `relates_to` (`structuring-epics`, `linking-epic-children`). Epic closes when `epic-status.py` says all linked children closed.
- **Hygiene:** take → `In Work` + assignee self · MR opened → `Closes #NN`, assignee self · merged → close ticket with comment (MR link) · found already done → verify in code, close with pointer · obsolete → close with reason · side defect → new ticket · significant decision → comment on ticket (`recording-decisions`).
- Check every ticket against §1 boundary and `docs/USE_CASES.md` §4.1 first; out-of-boundary = question for the user, not a ticket.

## 8. How to work
1. **Understand before changing.** Read relevant files; ask if unclear.
2. **Plan first** for any non-trivial change: what, why, how tested.
3. **One scenario at a time** (team formation → task execution → acceptance/rework).
4. **Never invent features** outside §1. Unsure → ask.
5. **Server is the source of truth** for permissions; never move them to the client.
6. **After any substantive change, propose what to log in AI_USAGE.md** — rules in §10.
7. CLAUDE.md is a working document: durable lesson learned → propose adding it in the same MR. If CLAUDE.md disagrees with a stale doc or a hunch, CLAUDE.md wins until explicitly updated.

## 9. Skills & tools (`.claude/`)

| Doing | Skill / tool |
|---|---|
| Whole ticket, number → merged MR ("do #NN") | `ticket-to-release` · `/ticket <n>` |
| Filing a ticket | `creating-gitlab-issue` → `tools/issue-create.py` |
| Creating/updating an epic or its child | `structuring-epics` |
| Linking children / epic closure | `linking-epic-children` → `tools/epic-link.py`, `tools/epic-status.py` |
| "Is it done?" before MR/close | `defining-done` |
| Logging AI usage | `ai-usage-report` |
| New behavior / bugfix | `superpowers:test-driven-development` |
| Waiting on background process | `Monitor` tool, never a hand-made `sleep` loop |

Before creating a new skill/tool: grep existing ones for the same trigger; prefer narrow single-purpose skills sharing a private helper (`_glab.py`); state the boundary with adjacent skills.

## 10. AI usage log — [AI_USAGE.md](AI_USAGE.md)

Course requirement. After any substantive change, propose an entry (skill `ai-usage-report`; a PostToolUse hook reminds on every Edit/Write). Read the current file first; update a related entry instead of duplicating.
- **Log** if AI influenced: meaning of requirements/documents, chosen solution, code behavior, test content, analysis conclusions.
- **Do not log:** typos, formatting, single-term translation, short autocomplete, reference questions with no substantive impact.
- **Entry fields (Russian, all required):** Участник (`M3`/`A100`/`E95`, codes from PROJECT.md; ask if unknown) · Задача · Использование ИИ · В проект вошло (precise: "переработанная версия", not "сгенерировано") · Проверка человеком (only what was really done — never invent verification) · Связанный результат (file, SR-*, T-*, D-*, commit, MR, test).
- No full names, group numbers or personal emails — codes only.
- The "Инфраструктура ИИ" section (tool, CLAUDE.md, skills, hooks) is declared once; touch it only when tooling actually changes (skill/hook added or removed), never fold it into a task entry.
- Ask the user for participant, what was verified and the result link before writing; the human owns verification and responsibility for what enters the project.

## 11. Hard rules for the agent
- No code you cannot explain line by line. No silent rewrites of unrelated code.
- No new dependencies without asking.
- Do not touch secrets, credentials, personal data; never print secret values (`handling-secrets`). Hook `guard_env.py` blocks agent access to real `.env` files; new keys go to `.env.example`.
- Do not claim a test passed unless it was run.
- Auth is required for every action; comments, attachments and real-time events are visible only to the task's team members (check membership server-side). Only the author accepts or returns a task.
- Ambiguous task → stop and ask.
- **No code comments** except where the *why* is non-obvious; prefer self-explanatory names.
