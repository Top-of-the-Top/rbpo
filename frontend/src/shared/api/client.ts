import createClient from 'openapi-fetch'

import { env } from '@/shared/config'

import { createAuthMiddleware } from './authMiddleware'
import type { paths } from './schema.gen'

const PUBLIC_PREFIX = '/api/auth/'

/** Типизированный клиент: пути, тела и ответы берутся из schema.gen.ts (npm run api:sync). */
export const api = createClient<paths>({ baseUrl: env.apiBaseUrl })

const auth = createAuthMiddleware({ isPublicPath: (path) => path.startsWith(PUBLIC_PREFIX) })
api.use(auth.middleware)

export const configureAuth = auth.configure
