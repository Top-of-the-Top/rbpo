import { LogOutIcon } from 'lucide-react'

import { Button } from '@/shared/ui/button'
import { Spinner } from '@/shared/ui/spinner'

import { useLogout } from '../model/useLogout'

export function LogoutButton() {
  const logout = useLogout()

  return (
    <Button variant="ghost" size="sm" disabled={logout.isPending} onClick={() => logout.mutate()}>
      {logout.isPending ? <Spinner /> : <LogOutIcon />}
      Выйти
    </Button>
  )
}
