import { decodeJwtPayload } from '@/shared/lib'

import type { Viewer } from '../model/types'

/** Кто вошёл — из claims access-токена (`sub`, `username`). Только для отображения. */
export function decodeViewer(accessToken: string): Viewer | null {
  const payload = decodeJwtPayload(accessToken)
  const id = payload?.sub
  const username = payload?.username
  return typeof id === 'string' && typeof username === 'string' ? { id, username } : null
}
