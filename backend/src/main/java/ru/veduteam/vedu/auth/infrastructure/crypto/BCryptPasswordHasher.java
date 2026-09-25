package ru.veduteam.vedu.auth.infrastructure.crypto;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import ru.veduteam.vedu.auth.application.PasswordHasher;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
  private static final int MAX_PASSWORD_BYTES = 72;

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String passwordHash) {
    if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
      return false;
    }
    return encoder.matches(rawPassword, passwordHash);
  }
}
