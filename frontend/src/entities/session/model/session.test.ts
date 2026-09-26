import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import type { TokenPair } from '../api/authApi'

const authApi = vi.hoisted(() => ({
  login: vi.fn(),
  register: vi.fn(),
  refresh: vi.fn(),
  logout: vi.fn(),
}))
vi.mock('../api/authApi', () => ({ authApi }))

const REFRESH_KEY = 'vedu.refreshToken'

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: object) =>
    btoa(JSON.stringify(value)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_')
  return `${encode({ alg: 'HS256' })}.${encode(payload)}.signature`
}

const pair = (n: number): TokenPair => ({
  accessToken: fakeJwt({ sub: 'user-1', username: 'alice', type: 'access', n }),
  refreshToken: `refresh-${n}`,
})

function memoryStorage(): Storage {
  const data = new Map<string, string>()
  return {
    get length() {
      return data.size
    },
    clear: () => data.clear(),
    getItem: (key) => data.get(key) ?? null,
    key: (index) => [...data.keys()][index] ?? null,
    removeItem: (key) => void data.delete(key),
    setItem: (key, value) => void data.set(key, value),
  }
}

// Модуль сессии хранит состояние на уровне модуля — в каждом тесте загружаем его заново.
// ApiError берём из того же графа модулей, иначе instanceof в сессии не сработает.
let ApiError: typeof import('@/shared/api').ApiError

async function loadSession() {
  vi.resetModules()
  ;({ ApiError } = await import('@/shared/api'))
  return import('./session')
}

beforeEach(() => {
  vi.stubGlobal('localStorage', memoryStorage())
  Object.values(authApi).forEach((fn) => fn.mockReset())
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('session', () => {
  it('без сохранённого refresh стартует анонимно и не ходит на сервер', async () => {
    const session = await loadSession()
    await session.restoreSession()

    expect(session.sessionStore.getSnapshot()).toEqual({ status: 'anonymous', viewer: null })
    expect(authApi.refresh).not.toHaveBeenCalled()
  })

  it('signIn: access только в памяти, refresh в storage, viewer из claims', async () => {
    const session = await loadSession()
    session.signIn(pair(1))

    expect(session.getAccessToken()).toBe(pair(1).accessToken)
    expect(localStorage.getItem(REFRESH_KEY)).toBe('refresh-1')
    expect(session.sessionStore.getSnapshot()).toEqual({
      status: 'authenticated',
      viewer: { id: 'user-1', username: 'alice' },
    })
  })

  it('восстанавливает сессию по сохранённому refresh и сохраняет ротированный токен', async () => {
    localStorage.setItem(REFRESH_KEY, 'refresh-1')
    authApi.refresh.mockResolvedValue(pair(2))
    const session = await loadSession()

    await session.restoreSession()

    expect(authApi.refresh).toHaveBeenCalledWith('refresh-1')
    expect(session.sessionStore.getSnapshot().status).toBe('authenticated')
    expect(localStorage.getItem(REFRESH_KEY)).toBe('refresh-2')
  })

  it('параллельные refresh превращаются в один запрос', async () => {
    localStorage.setItem(REFRESH_KEY, 'refresh-1')
    authApi.refresh.mockResolvedValue(pair(2))
    const session = await loadSession()

    const tokens = await Promise.all([session.refreshSession(), session.refreshSession(), session.refreshSession()])

    expect(authApi.refresh).toHaveBeenCalledOnce()
    expect(new Set(tokens)).toEqual(new Set([pair(2).accessToken]))
  })

  it('отозванный refresh (401) — выход и очистка storage', async () => {
    localStorage.setItem(REFRESH_KEY, 'refresh-1')
    const session = await loadSession()
    authApi.refresh.mockRejectedValue(new ApiError(401, null))

    await expect(session.refreshSession()).resolves.toBeNull()
    expect(session.sessionStore.getSnapshot().status).toBe('anonymous')
    expect(localStorage.getItem(REFRESH_KEY)).toBeNull()
  })

  it('сетевая ошибка при refresh не стирает refresh-токен', async () => {
    localStorage.setItem(REFRESH_KEY, 'refresh-1')
    const session = await loadSession()
    authApi.refresh.mockRejectedValue(new ApiError(0, null))

    await session.restoreSession()

    expect(session.sessionStore.getSnapshot().status).toBe('anonymous')
    expect(localStorage.getItem(REFRESH_KEY)).toBe('refresh-1')
  })

  it('signOut отзывает refresh на сервере и чистит сессию', async () => {
    authApi.logout.mockResolvedValue(undefined)
    const session = await loadSession()
    session.signIn(pair(1))

    await session.signOut()

    expect(authApi.logout).toHaveBeenCalledWith('refresh-1')
    expect(session.getAccessToken()).toBeNull()
    expect(localStorage.getItem(REFRESH_KEY)).toBeNull()
    expect(session.sessionStore.getSnapshot().status).toBe('anonymous')
  })

  it('signOut чистит сессию локально, даже если сервер недоступен', async () => {
    const session = await loadSession()
    authApi.logout.mockRejectedValue(new ApiError(0, null))
    session.signIn(pair(1))

    await session.signOut()

    expect(localStorage.getItem(REFRESH_KEY)).toBeNull()
    expect(session.sessionStore.getSnapshot().status).toBe('anonymous')
  })

  it('работает, когда localStorage недоступен', async () => {
    vi.stubGlobal('localStorage', {
      getItem: () => {
        throw new Error('SecurityError')
      },
      setItem: () => {
        throw new Error('SecurityError')
      },
      removeItem: () => {
        throw new Error('SecurityError')
      },
    })
    const session = await loadSession()

    expect(() => session.signIn(pair(1))).not.toThrow()
    expect(session.sessionStore.getSnapshot().status).toBe('authenticated')
    await expect(session.restoreSession()).resolves.toBeUndefined()
  })
})
