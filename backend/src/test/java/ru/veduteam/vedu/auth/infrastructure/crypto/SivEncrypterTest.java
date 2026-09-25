package ru.veduteam.vedu.auth.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import ru.veduteam.vedu.auth.application.errors.DecryptionException;
import ru.veduteam.vedu.auth.application.errors.InvalidEncryptionKeyException;

class SivEncrypterTest {
  private static final String KEY = key(1);
  private static final String IV_KEY = key(2);

  private static String key(int seed) {
    byte[] bytes = new byte[32];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) (seed * 31 + i);
    }
    return Base64.getEncoder().encodeToString(bytes);
  }

  @Test
  void decryptReturnsOriginalValue() {
    SivEncrypter encrypter = new SivEncrypter(KEY, IV_KEY);

    String encrypted = encrypter.encrypt("user@example.com");

    assertThat(encrypted).isNotEqualTo("user@example.com");
    assertThat(encrypter.decrypt(encrypted)).isEqualTo("user@example.com");
  }

  @Test
  void sameValueEncryptsIdentically() {
    SivEncrypter encrypter = new SivEncrypter(KEY, IV_KEY);

    assertThat(encrypter.encrypt("user@example.com")).isEqualTo(encrypter.encrypt("user@example.com"));
  }

  @Test
  void differentValuesUseDifferentIvs() {
    SivEncrypter encrypter = new SivEncrypter(KEY, IV_KEY);

    byte[] first = Base64.getDecoder().decode(encrypter.encrypt("alice@example.com"));
    byte[] second = Base64.getDecoder().decode(encrypter.encrypt("bob@example.com"));

    assertThat(Arrays.copyOf(first, 12)).isNotEqualTo(Arrays.copyOf(second, 12));
  }

  @Test
  void differentIvKeyGivesDifferentCiphertext() {
    String first = new SivEncrypter(KEY, IV_KEY).encrypt("user@example.com");
    String second = new SivEncrypter(KEY, key(3)).encrypt("user@example.com");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void tamperedValueIsRejected() {
    SivEncrypter encrypter = new SivEncrypter(KEY, IV_KEY);
    byte[] data = Base64.getDecoder().decode(encrypter.encrypt("user@example.com"));
    data[data.length - 1] ^= 1;

    assertThatThrownBy(() -> encrypter.decrypt(Base64.getEncoder().encodeToString(data)))
        .isInstanceOf(DecryptionException.class);
  }

  @Test
  void valueEncryptedWithAnotherKeyIsRejected() {
    String encrypted = new SivEncrypter(KEY, IV_KEY).encrypt("user@example.com");

    assertThatThrownBy(() -> new SivEncrypter(key(4), IV_KEY).decrypt(encrypted))
        .isInstanceOf(DecryptionException.class);
  }

  @Test
  void malformedValueIsRejected() {
    SivEncrypter encrypter = new SivEncrypter(KEY, IV_KEY);

    assertThatThrownBy(() -> encrypter.decrypt("not base64!")).isInstanceOf(DecryptionException.class);
    assertThatThrownBy(() -> encrypter.decrypt("AAAA")).isInstanceOf(DecryptionException.class);
  }

  @Test
  void keyOfWrongLengthIsRejected() {
    String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

    assertThatThrownBy(() -> new SivEncrypter(shortKey, IV_KEY))
        .isInstanceOf(InvalidEncryptionKeyException.class);
    assertThatThrownBy(() -> new SivEncrypter(KEY, shortKey))
        .isInstanceOf(InvalidEncryptionKeyException.class);
  }

  @Test
  void missingOrNonBase64KeyIsRejected() {
    assertThatThrownBy(() -> new SivEncrypter(null, IV_KEY))
        .isInstanceOf(InvalidEncryptionKeyException.class);
    assertThatThrownBy(() -> new SivEncrypter(KEY, "not base64!"))
        .isInstanceOf(InvalidEncryptionKeyException.class);
  }
}
