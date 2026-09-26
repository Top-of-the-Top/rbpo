import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'

import { bootstrap } from './bootstrap'
import { AppProviders } from './setup/AppProviders'
import { router } from './router/router'
import './styles/index.css'

bootstrap()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  </StrictMode>,
)
