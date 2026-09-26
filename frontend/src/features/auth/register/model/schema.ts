import { z } from 'zod'

const utf8Length = (value: string) => new TextEncoder().encode(value).length

// Лимиты повторяют RegisterRequest на бэке (bcrypt режет пароль после 72 байт) — для подсказок; проверяет сервер.
export const registerSchema = z.object({
  username: z
    .string()
    .min(3, 'От 3 символов')
    .max(50, 'Не длиннее 50 символов')
    .regex(/^[a-zA-Z0-9_.-]+$/, 'Только латиница, цифры и символы _ . -'),
  email: z.email('Некорректный адрес почты').max(254, 'Слишком длинный адрес'),
  password: z
    .string()
    .min(8, 'Не короче 8 символов')
    .refine((value) => utf8Length(value) <= 72, 'Слишком длинный пароль'),
})

export type RegisterValues = z.infer<typeof registerSchema>
