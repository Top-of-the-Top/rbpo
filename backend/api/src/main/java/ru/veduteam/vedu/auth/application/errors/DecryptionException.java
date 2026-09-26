package ru.veduteam.vedu.auth.application.errors;

public class DecryptionException extends EncryptionException {
  public DecryptionException(String message) {
    super(message);
  }

  public DecryptionException(String message, Throwable cause) {
    super(message, cause);
  }
}
