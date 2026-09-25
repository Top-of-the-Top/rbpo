package ru.veduteam.vedu.auth.infrastructure.http;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.veduteam.vedu.auth.application.AuthService;
import ru.veduteam.vedu.auth.application.dto.LoginRequest;
import ru.veduteam.vedu.auth.application.dto.RefreshTokenRequest;
import ru.veduteam.vedu.auth.application.dto.RegisterRequest;
import ru.veduteam.vedu.auth.application.dto.TokenPair;

@RestController
@RequestMapping("/api/auth")
public final class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public TokenPair login(@Valid @RequestBody LoginRequest request) {
    return service.login(request);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody RefreshTokenRequest request) {
    service.logout(request);
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public TokenPair register(@Valid @RequestBody RegisterRequest request) {
    return service.register(request);
  }

  @PostMapping("/refresh")
  public TokenPair refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return service.refresh(request);
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