import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'

import { Alert, AlertDescription } from '@/shared/ui/alert'
import { Button } from '@/shared/ui/button'
import { Field, FieldError, FieldGroup, FieldLabel } from '@/shared/ui/field'
import { Input } from '@/shared/ui/input'
import { Spinner } from '@/shared/ui/spinner'

import { loginSchema, type LoginValues } from '../model/schema'
import { loginErrorMessage, useLogin } from '../model/useLogin'

// После успешного входа редиректит guard маршрута (RequireGuest), форма об этом не знает.
export function LoginForm() {
  const login = useLogin()
  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: '', password: '' },
  })

  return (
    <form noValidate onSubmit={form.handleSubmit((values) => login.mutate(values))}>
      <FieldGroup>
        {login.isError && (
          <Alert variant="destructive">
            <AlertDescription>{loginErrorMessage(login.error)}</AlertDescription>
          </Alert>
        )}

        <Controller
          name="username"
          control={form.control}
          render={({ field, fieldState }) => (
            <Field data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="login-username">Логин</FieldLabel>
              <Input {...field} id="login-username" autoComplete="username" aria-invalid={fieldState.invalid} />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />

        <Controller
          name="password"
          control={form.control}
          render={({ field, fieldState }) => (
            <Field data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="login-password">Пароль</FieldLabel>
              <Input
                {...field}
                id="login-password"
                type="password"
                autoComplete="current-password"
                aria-invalid={fieldState.invalid}
              />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />

        <Button type="submit" disabled={login.isPending}>
          {login.isPending && <Spinner />}
          Войти
        </Button>
      </FieldGroup>
    </form>
  )
}
