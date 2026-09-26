import { useMutation } from '@tanstack/react-query'

import { authApi, signIn } from '@/entities/session'
import { getErrorMessage } from '@/shared/api'

import type { LoginValues } from './schema'

/**
 * Шаг «логин + пароль» (UC-02). Когда бэк включит одноразовый код (/api/auth/verify),
 * ответ login станет «нужен код», и здесь появится второй шаг вместо немедленного signIn.
 */
export function useLogin() {
  return useMutation({
    mutationFn: (values: LoginValues) => authApi.login(values),
    onSuccess: signIn,
  })
}

// UC-02 2а: одно сообщение и для неизвестного логина, и для неверного пароля.
export const loginErrorMessage = (error: unknown) =>
  getErrorMessage(error, { 401: 'Неверный логин или пароль', 429: 'Слишком много попыток. Попробуйте позже' })
