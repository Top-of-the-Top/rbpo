import { QueryClient } from '@tanstack/react-query'

import { isApiError } from './errors'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // 4xx повторять бессмысленно: ответ не изменится.
      retry: (failureCount, error) =>
        failureCount < 2 && !(isApiError(error) && error.status >= 400 && error.status < 500),
    },
    mutations: {
      retry: false,
    },
  },
})
