import { Link } from 'react-router'

import { routes } from '@/shared/config'
import { Button } from '@/shared/ui/button'

export function NotFoundPage() {
  return (
    <main className="flex min-h-svh flex-col items-center justify-center gap-4 p-4 text-center">
      <h1 className="text-2xl font-semibold">Страница не найдена</h1>
      <Button asChild variant="outline">
        <Link to={routes.home}>На главную</Link>
      </Button>
    </main>
  )
}
