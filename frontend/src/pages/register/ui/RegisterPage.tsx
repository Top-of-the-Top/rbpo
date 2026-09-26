import { Link } from 'react-router'

import { RegisterForm } from '@/features/auth/register'
import { routes } from '@/shared/config'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/shared/ui/card'

export function RegisterPage() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Регистрация</CardTitle>
        <CardDescription>Создайте аккаунт, чтобы работать с командой</CardDescription>
      </CardHeader>
      <CardContent>
        <RegisterForm />
      </CardContent>
      <CardFooter className="justify-center text-sm text-muted-foreground">
        Уже есть аккаунт?&nbsp;
        <Link to={routes.login} className="text-foreground underline underline-offset-4">
          Войти
        </Link>
      </CardFooter>
    </Card>
  )
}
