package ru.veduteam.vedu.auth.application.errors;

public class InvalidEncryptionKeyException extends EncryptionException {
  public InvalidEncryptionKeyException(String message) {
    super(message);
  }

  public InvalidEncryptionKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
