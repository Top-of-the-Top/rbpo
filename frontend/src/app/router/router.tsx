import { createBrowserRouter } from 'react-router'

import { HomePage } from '@/pages/home'
import { LoginPage } from '@/pages/login'
import { NotFoundPage } from '@/pages/not-found'
import { RegisterPage } from '@/pages/register'
import { routes } from '@/shared/config'

import { RequireAuth, RequireGuest } from './guards'
import { AppLayout, GuestLayout } from './layouts'

export const router = createBrowserRouter([
  {
    element: <RequireGuest />,
    children: [
      {
        element: <GuestLayout />,
        children: [
          { path: routes.login, element: <LoginPage /> },
          { path: routes.register, element: <RegisterPage /> },
        ],
      },
    ],
  },
  {
    element: <RequireAuth />,
    children: [
      {
        element: <AppLayout />,
        children: [{ path: routes.home, element: <HomePage /> }],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
