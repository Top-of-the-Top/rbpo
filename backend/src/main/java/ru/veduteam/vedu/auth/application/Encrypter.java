package ru.veduteam.vedu.auth.application;

public interface Encrypter {
  String encrypt(String value);

  String decrypt(String encryptedValue);
}
