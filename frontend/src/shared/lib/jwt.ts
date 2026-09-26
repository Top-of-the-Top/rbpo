/**
 * Декодирует payload JWT без проверки подписи.
 * Годится только для отображения (имя пользователя в шапке) —
 * никаких решений о правах на основе этих данных клиент не принимает.
 */
export function decodeJwtPayload(token: string): Record<string, unknown> | null {
  const payload = token.split('.')[1]
  if (!payload) return null

  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const bytes = Uint8Array.from(atob(padded), (char) => char.charCodeAt(0))
    const parsed: unknown = JSON.parse(new TextDecoder().decode(bytes))
    return typeof parsed === 'object' && parsed !== null ? (parsed as Record<string, unknown>) : null
  } catch {
    return null
  }
}
