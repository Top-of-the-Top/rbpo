/**
 * Единственное место, знающее, где лежит refresh-токен.
 * Сейчас бэк отдаёт его в теле ответа, поэтому localStorage (риск XSS — известный долг).
 * Переезд на httpOnly cookie = замена этого модуля и authApi, фичи не меняются.
 */
const KEY = 'vedu.refreshToken'

function storage(): Storage | null {
  try {
    return globalThis.localStorage ?? null
  } catch {
    return null
  }
}

export const tokenStorage = {
  get(): string | null {
    try {
      return storage()?.getItem(KEY) ?? null
    } catch {
      return null
    }
  },

  set(token: string): void {
    try {
      storage()?.setItem(KEY, token)
    } catch {
      // Хранилище недоступно (приватный режим): сессия проживёт до перезагрузки.
    }
  },

  clear(): void {
    try {
      storage()?.removeItem(KEY)
    } catch {
      // см. set
    }
  },

  /** Изменения из других вкладок (событие storage не приходит во вкладку, которая писала). */
  subscribe(listener: (token: string | null) => void): () => void {
    if (typeof window === 'undefined') return () => {}
    const onStorage = (event: StorageEvent) => {
      if (event.key === KEY || event.key === null) listener(tokenStorage.get())
    }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  },
}
