/** RFC 9457 Problem Details — формат ошибок Spring (`ProblemDetail`). */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  [extension: string]: unknown
}

/** Единый тип ошибки API для UI. `status === 0` — запрос не дошёл до сервера. */
export class ApiError extends Error {
  readonly status: number
  readonly problem: ProblemDetail | null

  constructor(status: number, problem: ProblemDetail | null, message?: string) {
    super(message ?? problem?.detail ?? problem?.title ?? `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }

  get isNetworkError(): boolean {
    return this.status === 0
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

/**
 * Текст ошибки для пользователя. Фичи передают свои формулировки по HTTP-статусу,
 * остальное закрывается общими сообщениями. Текст бэка (detail) наружу не показываем:
 * он англоязычный и может раскрывать лишнее.
 */
export function getErrorMessage(error: unknown, byStatus: Partial<Record<number, string>> = {}): string {
  if (!isApiError(error)) return 'Что-то пошло не так. Попробуйте ещё раз'
  if (error.isNetworkError) return 'Сервер недоступен. Проверьте соединение и попробуйте ещё раз'
  return (
    byStatus[error.status] ??
    (error.status >= 500 ? 'Ошибка на сервере. Попробуйте позже' : 'Не удалось выполнить запрос. Проверьте данные')
  )
}

function asProblemDetail(body: unknown): ProblemDetail | null {
  return typeof body === 'object' && body !== null ? (body as ProblemDetail) : null
}

type ApiResult<T> = { data?: T; error?: unknown; response: Response }

/**
 * Превращает результат openapi-fetch в данные либо бросает ApiError.
 * Удобно для TanStack Query: queryFn/mutationFn получают обычный промис.
 */
export async function unwrap<T>(request: Promise<ApiResult<T>>): Promise<T> {
  let result: ApiResult<T>
  try {
    result = await request
  } catch (cause) {
    throw new ApiError(0, null, cause instanceof Error ? cause.message : 'Network error')
  }

  if (!result.response.ok) {
    throw new ApiError(result.response.status, asProblemDetail(result.error))
  }
  return result.data as T
}
