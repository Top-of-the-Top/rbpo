# Vedu — frontend

React 19 + TypeScript + Vite. Архитектура — [Feature-Sliced Design](https://feature-sliced.design/).

## Запуск

```bash
npm install
npm run dev        # http://localhost:5173, /api проксируется на VITE_PROXY_TARGET (по умолчанию :8080)
```

Бэк поднимается из корня репозитория: `docker compose up`.

## Скрипты

| Скрипт | Что делает |
|---|---|
| `dev` / `build` / `preview` | Vite |
| `typecheck` | `tsc -b` |
| `lint` | ESLint |
| `lint:fsd` | [Steiger](https://github.com/feature-sliced/steiger) — проверка границ слоёв FSD |
| `test` / `test:watch` | Vitest |
| `api:fetch` | скачать OpenAPI-спеку с бэка (`OPENAPI_URL`, по умолчанию `http://localhost:8080/v3/api-docs`) в `src/shared/api/openapi.json` |
| `api:gen` | сгенерировать типы `src/shared/api/schema.gen.ts` из снапшота |
| `api:sync` | `api:fetch` + `api:gen` |
| `api:check` | для CI: сгенерированные типы совпадают с закоммиченными |

## API-клиент

Типы генерирует [openapi-typescript](https://openapi-ts.dev/), запросы делает [openapi-fetch](https://openapi-ts.dev/openapi-fetch/) — пути, тела и ответы проверяются компилятором:

```ts
import { api, unwrap } from '@/shared/api'

const tokens = await unwrap(api.POST('/api/auth/login', { body: { username, password } }))
```

- `unwrap` превращает ответ в данные или бросает `ApiError` (`status`, `problem` — RFC 9457 ProblemDetail; `status === 0` — сеть).
- `getErrorMessage(error, { 401: '...' })` — текст для пользователя; текст ошибки бэка наружу не показывается.
- Middleware сам подставляет `Authorization: Bearer`, на 401 один раз обновляет сессию и повторяет запрос. Эндпоинты `/api/auth/*` публичные.

Когда меняется контракт бэка: `npm run api:sync`, закоммитить `openapi.json` и `schema.gen.ts` — изменения видны в диффе PR.

## Структура

```
src/
  app/        точка входа, провайдеры, роутер, guards, bootstrap (связывает сессию с api-клиентом)
  pages/      login, register, home, not-found
  widgets/    app-header
  features/   auth/login, auth/register, auth/logout
  entities/   session — токены, текущий пользователь, refresh
  shared/     api (клиент, ошибки, сгенерированные типы), config, lib, ui (shadcn/ui)
```

Импорты только вниз по слоям и только через `index.ts` слайса. `shared` не знает о сессии: `app/bootstrap.ts` передаёт api-клиенту `getAccessToken` / `refreshSession` / `onAuthFailure`.

UI-компоненты добавляются через `npx shadcn@latest add <component>` — они попадают в `src/shared/ui` (см. `components.json`).

## Сессия

- Access-токен хранится только в памяти, refresh-токен — в `localStorage` (бэк отдаёт его в теле ответа). Всё хранение сосредоточено в `entities/session/lib/tokenStorage.ts`: чтобы перейти на httpOnly cookie, достаточно поменять его и `authApi`.
- Refresh выполняется одним запросом внутри вкладки и под Web Lock между вкладками, потому что бэк ротирует refresh-токен.
- Выход в одной вкладке завершает сессию во всех вкладках, кэш запросов при этом очищается.
- Guards маршрутов нужны только для удобства. Права проверяет сервер.
