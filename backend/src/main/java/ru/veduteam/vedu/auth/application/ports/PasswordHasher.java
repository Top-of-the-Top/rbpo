package ru.veduteam.vedu.auth.application.ports;

public interface PasswordHasher {
  String hash(String rawPassword);

  boolean matches(String rawPassword, String passwordHash);
}
