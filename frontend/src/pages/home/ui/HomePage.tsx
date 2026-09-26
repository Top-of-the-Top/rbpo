import { useSession } from '@/entities/session'

// Заглушка защищённой зоны: сюда придёт список команд (UC-04…UC-07).
export function HomePage() {
  const { viewer } = useSession()

  return (
    <section className="space-y-2">
      <h1 className="text-2xl font-semibold tracking-tight">Мои команды</h1>
      <p className="text-muted-foreground">
        {viewer ? `Вы вошли как @${viewer.username}. ` : ''}Раздел команд пока в разработке.
      </p>
    </section>
  )
}
