import { Link } from 'react-router'

import { LoginForm } from '@/features/auth/login'
import { routes } from '@/shared/config'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/shared/ui/card'

export function LoginPage() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Вход в Vedu</CardTitle>
        <CardDescription>Введите логин и пароль</CardDescription>
      </CardHeader>
      <CardContent>
        <LoginForm />
      </CardContent>
      <CardFooter className="justify-center text-sm text-muted-foreground">
        Нет аккаунта?&nbsp;
        <Link to={routes.register} className="text-foreground underline underline-offset-4">
          Зарегистрироваться
        </Link>
      </CardFooter>
    </Card>
  )
}
