import { isApiError } from '@/shared/api'
import { singleFlight } from '@/shared/lib'

import { authApi, type TokenPair } from '../api/authApi'
import { decodeViewer } from '../lib/decodeViewer'
import { tokenStorage } from '../lib/tokenStorage'
import { withLock } from '../lib/withLock'
import type { SessionState } from './types'

// Access-токен живёт только в памяти модуля: не в React-state и не в storage.
let accessToken: string | null = null
let state: SessionState = { status: 'unknown', viewer: null }
const listeners = new Set<() => void>()

const ANONYMOUS: SessionState = { status: 'anonymous', viewer: null }

function setState(next: SessionState) {
  state = next
  listeners.forEach((listener) => listener())
}

function becomeAnonymous() {
  accessToken = null
  if (state.status !== 'anonymous') setState(ANONYMOUS)
}

export const sessionStore = {
  subscribe(listener: () => void): () => void {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  },
  getSnapshot: (): SessionState => state,
}

export function getAccessToken(): string | null {
  return accessToken
}

export function signIn(tokens: TokenPair): void {
  accessToken = tokens.accessToken
  tokenStorage.set(tokens.refreshToken)
  setState({ status: 'authenticated', viewer: decodeViewer(tokens.accessToken) })
}

/** Локально забыть сессию (без запроса на сервер). */
export function clearSession(): void {
  tokenStorage.clear()
  becomeAnonymous()
}

/**
 * Обменивает refresh на новую пару. Внутри вкладки — один запрос на всех,
 * между вкладками — под Web Lock, токен читается уже внутри блокировки.
 * Возвращает новый access или null, если сессии нет / она отозвана.
 */
export const refreshSession = singleFlight(() =>
  withLock('vedu-refresh', async (): Promise<string | null> => {
    const refreshToken = tokenStorage.get()
    if (!refreshToken) {
      clearSession()
      return null
    }

    try {
      const tokens = await authApi.refresh(refreshToken)
      signIn(tokens)
      return tokens.accessToken
    } catch (error) {
      // 401 — токен отозван или просрочен. Сетевая ошибка — не выход: refresh остаётся для следующей попытки.
      if (isApiError(error) && error.status === 401) clearSession()
      else becomeAnonymous()
      return null
    }
  }),
)

/** Вызывается один раз при старте приложения. */
export async function restoreSession(): Promise<void> {
  if (!tokenStorage.get()) {
    becomeAnonymous()
    return
  }
  await refreshSession()
}

/** Выход (UC-03): отзываем refresh на сервере, локально чистим в любом случае. */
export async function signOut(): Promise<void> {
  const refreshToken = tokenStorage.get()
  try {
    if (refreshToken) await authApi.logout(refreshToken)
  } catch {
    // Сервер недоступен — всё равно выходим локально; refresh истечёт по TTL.
  } finally {
    clearSession()
  }
}

/** Синхронизация вкладок: выход в одной — выход во всех; вход в одной — подхватываем в остальных. */
export function syncSessionAcrossTabs(): () => void {
  return tokenStorage.subscribe((refreshToken) => {
    if (!refreshToken) becomeAnonymous()
    else if (state.status === 'anonymous') void refreshSession()
  })
}
