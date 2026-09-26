import createClient from 'openapi-fetch'
import { describe, expect, it, vi } from 'vitest'

import { createAuthMiddleware, type AuthHandlers } from './authMiddleware'

// Схема-заглушка: защищённый и публичный эндпоинт.
interface TestPaths {
  '/api/teams': {
    post: {
      requestBody: { content: { 'application/json': { name: string } } }
      responses: { 200: { content: { 'application/json': { ok: boolean } } } }
    }
  }
  '/api/auth/login': {
    post: {
      responses: { 200: { content: { 'application/json': { ok: boolean } } } }
    }
  }
}

const json = (status: number, body: unknown = {}) =>
  new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })

function setup(handlers: Partial<AuthHandlers> = {}) {
  const calls: { url: string; auth: string | null; body: string }[] = []
  const responses: Response[] = []

  const fetchMock = vi.fn(async (request: Request) => {
    calls.push({ url: new URL(request.url).pathname, auth: request.headers.get('Authorization'), body: await request.text() })
    return responses.shift() ?? json(200, { ok: true })
  })

  const full: AuthHandlers = {
    getAccessToken: () => 'old-access',
    refreshSession: vi.fn(async () => 'new-access'),
    onAuthFailure: vi.fn(),
    ...handlers,
  }

  const auth = createAuthMiddleware({ isPublicPath: (path) => path.startsWith('/api/auth/'), fetch: fetchMock })
  auth.configure(full)

  const client = createClient<TestPaths>({ baseUrl: 'http://api.test', fetch: fetchMock })
  client.use(auth.middleware)

  return { client, calls, responses, handlers: full }
}

describe('authMiddleware', () => {
  it('добавляет Bearer к защищённым запросам', async () => {
    const { client, calls } = setup()
    await client.POST('/api/teams', { body: { name: 'A' } })
    expect(calls[0].auth).toBe('Bearer old-access')
  })

  it('не отправляет токен на публичные auth-эндпоинты', async () => {
    const { client, calls } = setup()
    await client.POST('/api/auth/login')
    expect(calls[0].auth).toBeNull()
  })

  it('на 401 обновляет сессию и повторяет запрос с тем же телом и новым токеном', async () => {
    const { client, calls, responses, handlers } = setup()
    responses.push(json(401))

    const { data, response } = await client.POST('/api/teams', { body: { name: 'A' } })

    expect(handlers.refreshSession).toHaveBeenCalledOnce()
    expect(response.status).toBe(200)
    expect(data).toEqual({ ok: true })
    expect(calls).toHaveLength(2)
    expect(calls[1]).toMatchObject({ auth: 'Bearer new-access', body: JSON.stringify({ name: 'A' }) })
  })

  it('если refresh не удался — сообщает о потере сессии и отдаёт исходный 401', async () => {
    const { client, calls, responses, handlers } = setup({ refreshSession: vi.fn(async () => null) })
    responses.push(json(401))

    const { response } = await client.POST('/api/teams', { body: { name: 'A' } })

    expect(response.status).toBe(401)
    expect(handlers.onAuthFailure).toHaveBeenCalledOnce()
    expect(calls).toHaveLength(1)
  })

  it('401 от публичного эндпоинта (неверный пароль) не запускает refresh', async () => {
    const { client, responses, handlers } = setup()
    responses.push(json(401))

    const { response } = await client.POST('/api/auth/login')

    expect(response.status).toBe(401)
    expect(handlers.refreshSession).not.toHaveBeenCalled()
    expect(handlers.onAuthFailure).not.toHaveBeenCalled()
  })

  it('повторный 401 после refresh — сессия потеряна, без бесконечного цикла', async () => {
    const { client, calls, responses, handlers } = setup()
    responses.push(json(401), json(401))

    const { response } = await client.POST('/api/teams', { body: { name: 'A' } })

    expect(response.status).toBe(401)
    expect(calls).toHaveLength(2)
    expect(handlers.onAuthFailure).toHaveBeenCalledOnce()
  })
})
