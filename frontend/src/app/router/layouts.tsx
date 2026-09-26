import { Outlet } from 'react-router'

import { AppHeader } from '@/widgets/app-header'

export function AppLayout() {
  return (
    <div className="min-h-svh">
      <AppHeader />
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}

export function GuestLayout() {
  return (
    <main className="flex min-h-svh items-center justify-center bg-muted/40 p-4">
      <div className="w-full max-w-sm">
        <Outlet />
      </div>
    </main>
  )
}
