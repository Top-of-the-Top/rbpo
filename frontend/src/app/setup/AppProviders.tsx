import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import type { ReactNode } from 'react'

import { queryClient } from '@/shared/api'
import { env } from '@/shared/config'
import { Toaster } from '@/shared/ui/sonner'

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <Toaster />
      {env.isDev && <ReactQueryDevtools buttonPosition="bottom-left" />}
    </QueryClientProvider>
  )
}
