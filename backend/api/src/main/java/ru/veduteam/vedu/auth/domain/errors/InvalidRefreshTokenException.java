package ru.veduteam.vedu.auth.domain.errors;

public class InvalidRefreshTokenException extends RuntimeException {
  public InvalidRefreshTokenException() {
    super("Refresh token is invalid or expired");
  }
}
