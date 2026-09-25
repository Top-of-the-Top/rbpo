package ru.veduteam.vedu.auth.domain.errors;

public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException() {
    super("Invalid username or password");
  }
}
