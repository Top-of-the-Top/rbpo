/** Параллельные вызовы, пока предыдущий не завершился, получают один и тот же промис. */
export function singleFlight<T>(fn: () => Promise<T>): () => Promise<T> {
  let inFlight: Promise<T> | null = null

  return () => {
    inFlight ??= fn().finally(() => {
      inFlight = null
    })
    return inFlight
  }
}
