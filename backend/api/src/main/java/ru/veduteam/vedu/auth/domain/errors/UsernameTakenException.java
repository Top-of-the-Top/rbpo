package ru.veduteam.vedu.auth.domain.errors;

public class UsernameTakenException extends RuntimeException {
  public UsernameTakenException() {
    super("Username is already taken");
  }
}
