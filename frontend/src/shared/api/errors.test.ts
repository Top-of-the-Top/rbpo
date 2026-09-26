import { describe, expect, it } from 'vitest'

import { ApiError, getErrorMessage, unwrap } from './errors'

describe('unwrap', () => {
  it('возвращает data для успешного ответа', async () => {
    const data = await unwrap(Promise.resolve({ data: { a: 1 }, response: new Response(null, { status: 200 }) }))
    expect(data).toEqual({ a: 1 })
  })

  it('превращает ProblemDetail в ApiError со статусом', async () => {
    const problem = { status: 409, title: 'Conflict', detail: 'Username is already taken' }
    const error = await unwrap(
      Promise.resolve({ error: problem, response: new Response(null, { status: 409 }) }),
    ).catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 409, problem, message: 'Username is already taken' })
  })

  it('сетевую ошибку превращает в ApiError со статусом 0', async () => {
    const error = await unwrap(Promise.reject(new TypeError('Failed to fetch'))).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).isNetworkError).toBe(true)
  })
})

describe('getErrorMessage', () => {
  it('берёт формулировку фичи по статусу', () => {
    expect(getErrorMessage(new ApiError(401, null), { 401: 'Неверный логин или пароль' })).toBe(
      'Неверный логин или пароль',
    )
  })

  it('не показывает текст бэка, а даёт общие сообщения', () => {
    expect(getErrorMessage(new ApiError(500, { detail: 'NullPointerException at ...' }))).toBe(
      'Ошибка на сервере. Попробуйте позже',
    )
    expect(getErrorMessage(new ApiError(0, null))).toMatch(/Сервер недоступен/)
    expect(getErrorMessage(new Error('boom'))).toMatch(/Что-то пошло не так/)
  })
})
