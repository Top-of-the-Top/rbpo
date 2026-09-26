import { Link } from 'react-router'

import { useSession } from '@/entities/session'
import { LogoutButton } from '@/features/auth/logout'
import { routes } from '@/shared/config'

export function AppHeader() {
  const { viewer } = useSession()

  return (
    <header className="border-b">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between gap-4 px-4">
        <Link to={routes.home} className="text-lg font-semibold tracking-tight">
          Vedu
        </Link>
        <div className="flex items-center gap-2">
          {viewer && <span className="text-sm text-muted-foreground">@{viewer.username}</span>}
          <LogoutButton />
        </div>
      </div>
    </header>
  )
}
