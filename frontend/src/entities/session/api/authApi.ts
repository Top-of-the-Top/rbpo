import { api, unwrap, type Schemas } from '@/shared/api'

export type TokenPair = Required<Schemas['TokenPair']>
export type LoginRequest = Schemas['LoginRequest']
export type RegisterRequest = Schemas['RegisterRequest']

export const authApi = {
  login: (body: LoginRequest) => unwrap(api.POST('/api/auth/login', { body })) as Promise<TokenPair>,
  register: (body: RegisterRequest) => unwrap(api.POST('/api/auth/register', { body })) as Promise<TokenPair>,
  refresh: (refreshToken: string) =>
    unwrap(api.POST('/api/auth/refresh', { body: { refreshToken } })) as Promise<TokenPair>,
  logout: (refreshToken: string) => unwrap(api.POST('/api/auth/logout', { body: { refreshToken } })),
}
