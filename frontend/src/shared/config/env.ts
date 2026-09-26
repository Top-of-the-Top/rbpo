// Единственная точка чтения import.meta.env: остальной код получает типизированные значения.
export const env = {
  /** Пустая строка = тот же origin (в dev запросы идут через vite proxy). */
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '',
  isDev: import.meta.env.DEV,
} as const
