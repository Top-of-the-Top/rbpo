/**
 * Web Locks сериализуют refresh между вкладками: бэк ротирует refresh-токен,
 * и без блокировки две вкладки обменяли бы один и тот же токен — вторая бы разлогинилась.
 */
export async function withLock<T>(name: string, fn: () => Promise<T>): Promise<T> {
  const locks = globalThis.navigator?.locks
  return locks ? locks.request(name, fn) : fn()
}
