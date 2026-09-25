package ru.veduteam.vedu.auth.infrastructure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.veduteam.vedu.auth.application.AuthService;

@RestController
@RequestMapping("/api/auth")
public final class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public void login(@RequestBody Object obj) {
    // Логинимся
  }

  @PostMapping("/logout")
  public void logout(@RequestBody Object obj) {
    // Логаутимся
  }

  @PostMapping("/register")
  public void register(@RequestBody Object obj) {
    // Регистрируемся
  }

  @GetMapping("/refresh")
  public void refresh(@RequestBody Object obj) {
    // Обновление токена
  }

  @PostMapping("/verify")
  public void verify(@RequestBody Object obj) {
    // Вводим код (двухвакторная) - он приходит на почту. Отдаем токен после этого
  }

  @PostMapping("/change-password")
  public void changePassword(@RequestBody Object obj) {
    // Обновление пароля
  }

  @PostMapping("/recover")
  public void recover(@RequestBody Object obj) {
    // сброс - оправляется ссылка
  }

  @PostMapping("/recover/set")
  public void set(@RequestBody Object obj) {
    // сброс - устанавливаем новый пароль
  }

}