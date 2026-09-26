import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'

import { Alert, AlertDescription } from '@/shared/ui/alert'
import { Button } from '@/shared/ui/button'
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/shared/ui/field'
import { Input } from '@/shared/ui/input'
import { Spinner } from '@/shared/ui/spinner'

import { registerSchema, type RegisterValues } from '../model/schema'
import { registerErrorMessage, useRegister } from '../model/useRegister'

const fields = [
  { name: 'username', label: 'Логин', type: 'text', autoComplete: 'username', hint: 'Виден участникам команд и в @упоминаниях' },
  { name: 'email', label: 'Почта', type: 'email', autoComplete: 'email', hint: 'Для одноразовых кодов входа' },
  { name: 'password', label: 'Пароль', type: 'password', autoComplete: 'new-password', hint: 'Не короче 8 символов' },
] as const

export function RegisterForm() {
  const register = useRegister()
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { username: '', email: '', password: '' },
  })

  return (
    <form noValidate onSubmit={form.handleSubmit((values) => register.mutate(values))}>
      <FieldGroup>
        {register.isError && (
          <Alert variant="destructive">
            <AlertDescription>{registerErrorMessage(register.error)}</AlertDescription>
          </Alert>
        )}

        {fields.map(({ name, label, type, autoComplete, hint }) => (
          <Controller
            key={name}
            name={name}
            control={form.control}
            render={({ field, fieldState }) => (
              <Field data-invalid={fieldState.invalid}>
                <FieldLabel htmlFor={`register-${name}`}>{label}</FieldLabel>
                <Input
                  {...field}
                  id={`register-${name}`}
                  type={type}
                  autoComplete={autoComplete}
                  aria-invalid={fieldState.invalid}
                />
                {fieldState.invalid ? (
                  <FieldError errors={[fieldState.error]} />
                ) : (
                  <FieldDescription>{hint}</FieldDescription>
                )}
              </Field>
            )}
          />
        ))}

        <Button type="submit" disabled={register.isPending}>
          {register.isPending && <Spinner />}
          Зарегистрироваться
        </Button>
      </FieldGroup>
    </form>
  )
}
