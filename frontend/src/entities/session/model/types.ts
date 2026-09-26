export interface Viewer {
  id: string
  username: string
}

export type SessionStatus = 'unknown' | 'authenticated' | 'anonymous'

export interface SessionState {
  status: SessionStatus
  viewer: Viewer | null
}
