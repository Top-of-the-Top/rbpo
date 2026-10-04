# Architecture

Source of truth for *where things live*. Product rules: PROJECT.md. Conventions: CLAUDE.md §3.

## Runtime topology (`docker-compose.yml`)

| Container | Image / build | Port | Networks | Talks to | Secrets it gets |
|---|---|---|---|---|---|
| `vedu-api` | `backend/` (arg `MODULE=api`) | 8080 | `vedu_network`, `vedu_internal` | postgres, redis, broker | whole `.env` (JWT, email crypto keys) |
| `vedu-worker` | `backend/` (arg `MODULE=worker`) | — | `vedu_internal` | redis (+ broker) | Redis vars only |
| `vedu-postgres` | `postgres:17-alpine`, volume `pgdata` | `${POSTGRES_PORT}` | `vedu_internal` | — | DB creds |
| `vedu-redis` | `redis:8-alpine`, volume `redisdata`, password required | `${REDIS_PORT}` | `vedu_internal` | — | Redis password |

`vedu_internal` is `internal: true` — no outbound internet; only `vedu-api` is reachable from outside. RabbitMQ is used by the code (`spring-boot-starter-amqp`) but is not yet in compose — add it there when the worker consumes.

Store roles: **Postgres** = users, teams, tasks, comments, attachment metadata (Flyway migrations in `backend/api/src/main/resources/db`, `ddl-auto: validate`). **Redis** = one-time codes (TTL 300s, 5 attempts), refresh tokens. **Broker** = async notifications (mail with OTP) api → worker. **Files** = attachment storage TBD (access only via server after membership check).

## Backend modules (`backend/`)

Gradle multi-module (`settings.gradle.kts`): `api`, `worker`. Root package `ru.veduteam.vedu`.

| Package (`api`) | Role |
|---|---|
| `auth/` | registration, login, OTP, JWT access/refresh, password hashing, email encryption |
| `user/` | user aggregate + lookup |
| `shared/broker/{api,internal}` | `BrokerClient` port + RabbitMQ implementation (internal) |
| *(planned)* `team/`, `task/`, `comment/`, `attachment/` | per PROJECT.md §4.1 groups B–H |

Each module: `domain/` → `application/{services,ports,dto,errors}` → `infrastructure/{http,errors,redis,crypto,persistence}` (inward-only dependencies; details CLAUDE.md §3).

`worker`: package `notifications/` — consumes broker messages, sends mail (SMTP in prod; log stub locally, PROJECT.md §9).

### Auth flow (implemented / in progress)
register → email verification code → login (login+password) → OTP code to email → `verify` → JWT access (15 min) + refresh (14 d, stored in Redis, rotated). Config: `application.yaml` (`jwt.*`, `otp.*`, `crypto.email.*`). Email is stored encrypted (deterministic SIV) so it can't be read from the DB; passwords are BCrypt-hashed (SR-08).

## Where to put what

| Adding… | Goes in |
|---|---|
| business rule, invariant, state machine | `<module>/domain/` |
| use-case orchestration | `<module>/application/services/` |
| anything the service needs from outside | interface in `application/ports/`, impl in `infrastructure/<tech>/` |
| REST endpoint | `infrastructure/http/` controller (thin) + DTO in `application/dto/` |
| exception → HTTP status | `infrastructure/errors/` advice |
| DB table | new Flyway file + JPA entity in `infrastructure/persistence/` |
| cross-module technical client | `shared/` (never business logic) |
| config key | `application.yaml` + `.env.example` (+ compose env if worker needs it) |
| test | mirror path under `src/test/java` (domain: plain JUnit; app: Mockito over ports; infra: slice tests) |

## Frontend (`frontend/`)
React + TS + Vite. Renders board/list; **never decides permissions** — shows server hints, handles rejected moves by restoring the card (PROJECT.md §6).

## Tooling (`.claude/`)
`skills/` procedures, `tools/` glab-based scripts (`issue-create`, `issue-note`, `epic-link`, `epic-status`, shared `_glab.py`), `hooks/guard_branch.py`, `commands/ticket.md`. See CLAUDE.md §9.
