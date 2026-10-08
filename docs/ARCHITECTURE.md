# Архитектура

Где что лежит в коде. Правила продукта: PROJECT.md. Соглашения по коду: CLAUDE.md §3.

## Окружение (`docker-compose.yml`)

| Контейнер | Образ / сборка | Порт | Сети | Говорит с | Секреты |
|---|---|---|---|---|---|
| `vedu-api` | `backend/`, `MODULE=api` | 8080 | `vedu_network`, `vedu_internal` | postgres, redis, брокер | весь `.env` (JWT, ключи шифрования почты) |
| `vedu-worker` | `backend/`, `MODULE=worker` | нет | `vedu_internal` | redis, брокер | только Redis |
| `vedu-postgres` | `postgres:17-alpine`, том `pgdata` | `${POSTGRES_PORT}` | `vedu_internal` | нет | учётные данные БД |
| `vedu-redis` | `redis:8-alpine`, том `redisdata`, пароль обязателен | `${REDIS_PORT}` | `vedu_internal` | нет | пароль Redis |
| `vedu-rabbitmq` | `rabbitmq:4-management-alpine`, том `rabbitmqdata` | `${RABBITMQ_PORT}` | `vedu_internal` | нет | пользователь и пароль RabbitMQ |

`vedu_internal`: `internal: true`, выхода в интернет нет, снаружи доступен только `vedu-api`. У RabbitMQ нет опубликованных портов. `api` публикует в `vedu.events` (очередь `vedu.email`, привязка `email.#`), получатель в `worker` не реализован.

Хранилища:

- **Postgres**: пользователи, команды, задачи, комментарии, метаданные вложений (Flyway в `backend/api/src/main/resources/db`, `ddl-auto: validate`)
- **Redis**: одноразовые коды (TTL 300 с, 5 попыток), refresh-токены
- **Брокер**: асинхронные письма с кодом, путь `api` → `worker`
- **Файлы**: хранилище вложений не определено, доступ только через сервер после проверки членства

## Бэкенд (`backend/`)

Gradle, модули `api`, `worker`, `shared` (клиент брокера и контракт сообщений для обоих). Корневой пакет `ru.veduteam.vedu`.

| Пакет (`api`) | Назначение |
|---|---|
| `auth/` | регистрация, вход, одноразовый код, JWT access/refresh, хэш пароля, шифрование почты |
| `user/` | агрегат пользователя, поиск |
| `team/`, `task/`, `comment/`, `attachment/` (план) | группы B–H из [USE_CASES.md](USE_CASES.md) |

Слои модуля: `domain/` → `application/{services,ports,dto,errors}` → `infrastructure/{http,errors,redis,crypto,persistence}`. Зависимости только внутрь. Подробнее: CLAUDE.md §3.

`worker`: пакет `notifications/`, читает брокер, шлёт письма (SMTP в проде, локально заглушка в лог, PROJECT.md §9).

### Аутентификация (готово / в процессе)

Регистрация → код на почту → вход (логин+пароль) → код на почту → `verify` → JWT: access 15 мин, refresh 14 дней (Redis, ротация). Настройки: `application.yaml` (`jwt.*`, `otp.*`, `crypto.email.*`). Почта зашифрована (детерминированный SIV), из БД не читается. Пароли: BCrypt (SR-08).

## Куда класть новый код

| Добавляете | Кладёте в |
|---|---|
| бизнес-правило, инвариант, конечный автомат | `<модуль>/domain/` |
| сценарий использования | `<модуль>/application/services/` |
| зависимость от внешнего | интерфейс в `application/ports/`, реализацию в `infrastructure/<tech>/` |
| REST-эндпоинт | контроллер в `infrastructure/http/`, DTO в `application/dto/` |
| исключение → HTTP-статус | `infrastructure/errors/` |
| таблицу БД | Flyway-файл и JPA-сущность в `infrastructure/persistence/` |
| клиент для нескольких модулей | `shared/` (без бизнес-логики) |
| ключ конфигурации | `application.yaml` и `.env.example` |
| тест | `src/test/java`: domain на JUnit, application на Mockito поверх портов, infrastructure slice-тестами |

## Фронтенд (`frontend/`)

React, TypeScript, Vite. Рисует доску и список. Права не решает: показывает подсказки сервера и откатывает карточку при отклонённом переносе (PROJECT.md §6).

## Инструменты (`.claude/`)

`skills/`: процедуры. `tools/`: скрипты на `glab` (`issue-create`, `issue-note`, `epic-link`, `epic-status`, общий `_glab.py`). `hooks/guard_branch.py`. `commands/ticket.md`. Подробнее: CLAUDE.md §9.
