// Скачивает OpenAPI-спеку с запущенного бэка и кладёт снапшот в репозиторий.
// Снапшот коммитится: сборка фронта не зависит от поднятого бэка,
// а изменения контракта видны в диффе PR.
import { writeFile } from 'node:fs/promises'

const url = process.env.OPENAPI_URL ?? 'http://localhost:8080/v3/api-docs'
const out = new URL('../src/shared/api/openapi.json', import.meta.url)

const response = await fetch(url).catch((error) => {
  console.error(`Cannot reach ${url}: ${error.message}. Is the backend running?`)
  process.exit(1)
})
if (!response.ok) {
  console.error(`GET ${url} -> ${response.status} ${response.statusText}`)
  process.exit(1)
}

const spec = await response.json()
await writeFile(out, JSON.stringify(spec, null, 2) + '\n')
console.log(`OpenAPI spec saved to ${out.pathname}`)
