import type { components } from './schema.gen'

export { api, configureAuth } from './client'
export type { AuthHandlers } from './authMiddleware'
export { ApiError, getErrorMessage, isApiError, unwrap } from './errors'
export type { ProblemDetail } from './errors'
export { queryClient } from './queryClient'
export type { components, paths } from './schema.gen'

/** DTO бэка по имени: `Schemas['TokenPair']`. */
export type Schemas = components['schemas']
