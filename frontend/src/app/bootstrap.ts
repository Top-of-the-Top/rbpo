import {
  clearSession,
  getAccessToken,
  refreshSession,
  restoreSession,
  sessionStore,
  syncSessionAcrossTabs,
} from '@/entities/session'
import { configureAuth, queryClient } from '@/shared/api'

/** Связывает сессию с api-клиентом и запускает восстановление сессии. Вызывается один раз до рендера. */
export function bootstrap(): void {
  configureAuth({ getAccessToken, refreshSession, onAuthFailure: clearSession })

  // Данные прошлого пользователя не должны пережить выход (в т.ч. выход из другой вкладки).
  let previousStatus = sessionStore.getSnapshot().status
  sessionStore.subscribe(() => {
    const { status } = sessionStore.getSnapshot()
    if (status === 'anonymous' && previousStatus === 'authenticated') queryClient.clear()
    previousStatus = status
  })

  syncSessionAcrossTabs()
  void restoreSession()
}
