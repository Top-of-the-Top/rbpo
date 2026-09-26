import type { Middleware } from 'openapi-fetch'

/**
 * Что api-клиенту нужно знать о сессии. Реализацию подставляет слой app
 * (из entities/session), поэтому shared не импортирует верхние слои.
 */
export interface AuthHandlers {
  getAccessToken: () => string | null
  /** Обновляет пару токенов; возвращает новый access или null, если сессии больше нет. */
  refreshSession: () => Promise<string | null>
  /** Сессию восстановить не удалось — нужно разлогинить пользователя. */
  onAuthFailure: () => void
}

interface AuthMiddlewareOptions {
  /** Эндпоинты, которые не требуют и не получают access-токен (login, refresh...). */
  isPublicPath: (schemaPath: string) => boolean
  fetch?: (request: Request) => Promise<Response>
}

export function createAuthMiddleware({ isPublicPath, fetch: fetchImpl = (r) => globalThis.fetch(r) }: AuthMiddlewareOptions) {
  let handlers: AuthHandlers | null = null
  // Копии исходных запросов: тело уже прочитано fetch'ем, а для повтора после refresh оно нужно снова.
  const pending = new Map<string, Request>()

  const middleware: Middleware = {
    onRequest({ request, schemaPath, id }) {
      if (!handlers || isPublicPath(schemaPath)) return undefined

      const token = handlers.getAccessToken()
      if (token) request.headers.set('Authorization', `Bearer ${token}`)
      pending.set(id, request.clone())
      return request
    },

    async onResponse({ response, schemaPath, id }) {
      const original = pending.get(id)
      pending.delete(id)
      if (response.status !== 401 || !handlers || !original || isPublicPath(schemaPath)) return undefined

      const token = await handlers.refreshSession().catch(() => null)
      if (!token) {
        handlers.onAuthFailure()
        return undefined
      }

      const retry = new Request(original)
      retry.headers.set('Authorization', `Bearer ${token}`)
      const retried = await fetchImpl(retry)
      if (retried.status === 401) handlers.onAuthFailure()
      return retried
    },

    onError({ id }) {
      pending.delete(id)
    },
  }

  return {
    middleware,
    configure(next: AuthHandlers) {
      handlers = next
    },
  }
}
