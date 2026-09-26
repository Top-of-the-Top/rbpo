import fsd from '@feature-sliced/steiger-plugin'
import { defineConfig } from 'steiger'

export default defineConfig([
  ...fsd.configs.recommended,
  {
    // Фичи auth пока используются по одному разу; с появлением OTP/команд ссылок станет больше.
    // Вернуть правило, когда проект подрастёт.
    rules: { 'fsd/insignificant-slice': 'off' },
  },
])
