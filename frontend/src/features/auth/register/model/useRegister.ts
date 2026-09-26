import { useMutation } from '@tanstack/react-query'

import { authApi, signIn } from '@/entities/session'
import { getErrorMessage } from '@/shared/api'

import type { RegisterValues } from './schema'

/**
 * Регистрация (UC-01). Сейчас бэк сразу выдаёт токены; когда появится подтверждение почты кодом,
 * здесь будет переход на шаг ввода кода вместо signIn.
 */
export function useRegister() {
  return useMutation({
    mutationFn: (values: RegisterValues) => authApi.register(values),
    onSuccess: signIn,
  })
}

export const registerErrorMessage = (error: unknown) =>
  getErrorMessage(error, { 409: 'Логин или почта уже заняты' })
