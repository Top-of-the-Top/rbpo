package ru.veduteam.vedu.auth.infrastructure.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.veduteam.vedu.auth.application.Encrypter;
import ru.veduteam.vedu.auth.application.errors.DecryptionException;
import ru.veduteam.vedu.auth.application.errors.EncryptionException;
import ru.veduteam.vedu.auth.application.errors.InvalidEncryptionKeyException;

@Component
public class SivEncrypter implements Encrypter {
  private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
  private static final String CIPHER_KEY_ALGORITHM = "AES";
  private static final String IV_MAC_ALGORITHM = "HmacSHA256";
  private static final int KEY_LENGTH_BYTES = 32;
  private static final int IV_LENGTH_BYTES = 12;
  private static final int TAG_LENGTH_BITS = 128;

  private final SecretKeySpec key;
  private final SecretKeySpec ivKey;

  public SivEncrypter(
      @Value("${crypto.email.encryption-key}") String base64Key,
      @Value("${crypto.email.iv-key}") String base64IvKey) {
    this.key = new SecretKeySpec(decodeKey("Encryption key", base64Key), CIPHER_KEY_ALGORITHM);
    this.ivKey = new SecretKeySpec(decodeKey("IV key", base64IvKey), IV_MAC_ALGORITHM);
  }

  @Override
  public String encrypt(String value) {
    byte[] plain = value.getBytes(StandardCharsets.UTF_8);

    try {
      byte[] iv = deriveIv(plain);
      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      byte[] encrypted = cipher.doFinal(plain);

      byte[] result = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
      return Base64.getEncoder().encodeToString(result);
    } catch (GeneralSecurityException e) {
      throw new EncryptionException("Failed to encrypt value", e);
    }
  }

  @Override
  public String decrypt(String encryptedValue) {
    byte[] data;
    try {
      data = Base64.getDecoder().decode(encryptedValue);
    } catch (IllegalArgumentException e) {
      throw new DecryptionException("Encrypted value is not valid Base64", e);
    }

    if (data.length <= IV_LENGTH_BYTES) {
      throw new DecryptionException("Encrypted value is too short");
    }
    byte[] iv = Arrays.copyOfRange(data, 0, IV_LENGTH_BYTES);

    try {
      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      byte[] decrypted = cipher.doFinal(data, IV_LENGTH_BYTES, data.length - IV_LENGTH_BYTES);
      return new String(decrypted, StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new DecryptionException("Failed to decrypt value", e);
    }
  }

  private byte[] deriveIv(byte[] plain) throws GeneralSecurityException {
    Mac mac = Mac.getInstance(IV_MAC_ALGORITHM);
    mac.init(ivKey);
    return Arrays.copyOf(mac.doFinal(plain), IV_LENGTH_BYTES);
  }

  private static byte[] decodeKey(String name, String base64Key) {
    if (base64Key == null || base64Key.isBlank()) {
      throw new InvalidEncryptionKeyException(name + " is not set");
    }

    byte[] keyBytes;
    try {
      keyBytes = Base64.getDecoder().decode(base64Key);
    } catch (IllegalArgumentException e) {
      throw new InvalidEncryptionKeyException(name + " must be Base64-encoded", e);
    }

    if (keyBytes.length != KEY_LENGTH_BYTES) {
      throw new InvalidEncryptionKeyException("%s must be %d bytes, got %d"
          .formatted(name, KEY_LENGTH_BYTES, keyBytes.length));
    }
    return keyBytes;
  }
}
