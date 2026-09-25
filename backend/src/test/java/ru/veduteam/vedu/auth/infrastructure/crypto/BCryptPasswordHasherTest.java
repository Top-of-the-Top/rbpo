package ru.veduteam.vedu.auth.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {
  private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

  @Test
  void hashIsNotThePasswordAndMatchesIt() {
    String hash = hasher.hash("correct-horse");

    assertThat(hash).isNotEqualTo("correct-horse");
    assertThat(hasher.matches("correct-horse", hash)).isTrue();
    assertThat(hasher.matches("wrong-horse", hash)).isFalse();
  }

  @Test
  void samePasswordGetsDifferentHashes() {
    assertThat(hasher.hash("correct-horse")).isNotEqualTo(hasher.hash("correct-horse"));
  }

  @Test
  void passwordLongerThan72BytesDoesNotMatch() {
    String hash = hasher.hash("correct-horse");

    assertThat(hasher.matches("я".repeat(40), hash)).isFalse();
  }
}
