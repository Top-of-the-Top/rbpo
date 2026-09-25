package ru.veduteam.vedu.auth.domain.errors;

public class EmailTakenException extends RuntimeException {
  public EmailTakenException() {
    super("Email is already registered");
  }
}
