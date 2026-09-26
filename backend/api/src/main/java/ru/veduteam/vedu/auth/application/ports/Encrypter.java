package ru.veduteam.vedu.auth.application.ports;

public interface Encrypter {
  String encrypt(String value);

  String decrypt(String encryptedValue);
}
