import { Navigate, Outlet, useLocation, type Location } from 'react-router'

import { useSession } from '@/entities/session'
import { routes } from '@/shared/config'
import { Spinner } from '@/shared/ui/spinner'

type RedirectState = { from?: Location } | null

function FullscreenSpinner() {
  return (
    <div className="flex min-h-svh items-center justify-center">
      <Spinner className="size-6 text-muted-foreground" />
    </div>
  )
}

/**
 * Только для вошедших. Это UX, а не защита: права проверяет сервер на каждом запросе.
 * Запомненный адрес возвращает пользователя туда, куда он шёл, после входа.
 */
export function RequireAuth() {
  const { status } = useSession()
  const location = useLocation()

  if (status === 'unknown') return <FullscreenSpinner />
  if (status === 'anonymous') return <Navigate to={routes.login} replace state={{ from: location }} />
  return <Outlet />
}

/** Только для гостей (вход, регистрация). После входа уводит на исходный адрес или на главную. */
export function RequireGuest() {
  const { status } = useSession()
  const location = useLocation()
  const from = (location.state as RedirectState)?.from

  if (status === 'unknown') return <FullscreenSpinner />
  if (status === 'authenticated') {
    return <Navigate to={from ? `${from.pathname}${from.search}${from.hash}` : routes.home} replace />
  }
  return <Outlet />
}
