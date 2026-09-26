import { useSyncExternalStore } from 'react'

import { sessionStore } from './session'
import type { SessionState } from './types'

export function useSession(): SessionState {
  return useSyncExternalStore(sessionStore.subscribe, sessionStore.getSnapshot)
}
