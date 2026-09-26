import { z } from 'zod'

// Лимиты повторяют LoginRequest на бэке — только ради подсказок в форме; проверяет сервер.
export const loginSchema = z.object({
  username: z.string().min(1, 'Введите логин').max(50, 'Не длиннее 50 символов'),
  password: z.string().min(1, 'Введите пароль').max(72, 'Не длиннее 72 символов'),
})

export type LoginValues = z.infer<typeof loginSchema>
