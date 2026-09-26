import { useMutation } from '@tanstack/react-query'

import { signOut } from '@/entities/session'

/**
 * Выход (UC-03). signOut не падает: при недоступном сервере сессия всё равно чистится локально.
 * Кэш запросов чистит app (подписка на сессию), на /login уводит guard маршрута.
 */
export function useLogout() {
  return useMutation({ mutationFn: signOut })
}
