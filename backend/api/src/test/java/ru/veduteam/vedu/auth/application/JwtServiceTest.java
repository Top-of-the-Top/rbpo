package ru.veduteam.vedu.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ru.veduteam.vedu.auth.application.dto.RefreshTokenClaims;
import ru.veduteam.vedu.auth.application.dto.UserDetails;
import ru.veduteam.vedu.auth.application.services.JwtService;
import ru.veduteam.vedu.auth.domain.errors.InvalidRefreshTokenException;

class JwtServiceTest {
  private static final UserDetails USER = new UserDetails(UUID.randomUUID(), "bob");

  static JwtService jwtService(byte secretSeed, long refreshExpirationMillis) {
    byte[] secret = new byte[32];
    Arrays.fill(secret, secretSeed);

    JwtService service = new JwtService();
    ReflectionTestUtils.setField(service, "secretKey", Base64.getEncoder().encodeToString(secret));
    ReflectionTestUtils.setField(service, "accessTokenExpiration", 900_000L);
    ReflectionTestUtils.setField(service, "refreshTokenExpiration", refreshExpirationMillis);
    return service;
  }

  private final JwtService service = jwtService((byte) 1, 1_209_600_000L);

  @Test
  void refreshTokenIsParsedBackToUserAndTokenId() {
    String token = service.generateRefreshToken(USER, "token-1");

    RefreshTokenClaims claims = service.parseRefreshToken(token);

    assertThat(claims.userId()).isEqualTo(USER.id());
    assertThat(claims.tokenId()).isEqualTo("token-1");
  }

  @Test
  void refreshTtlComesFromConfig() {
    assertThat(service.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(14));
  }

  @Test
  void accessTokenIsNotAcceptedAsRefreshToken() {
    String accessToken = service.generateAccessToken(USER);

    assertThatThrownBy(() -> service.parseRefreshToken(accessToken))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void tokenSignedWithAnotherSecretIsRejected() {
    String foreignToken = jwtService((byte) 2, 1_209_600_000L).generateRefreshToken(USER, "token-1");

    assertThatThrownBy(() -> service.parseRefreshToken(foreignToken))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void expiredTokenIsRejected() {
    String expiredToken = jwtService((byte) 1, -1_000L).generateRefreshToken(USER, "token-1");

    assertThatThrownBy(() -> service.parseRefreshToken(expiredToken))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void garbageIsRejected() {
    assertThatThrownBy(() -> service.parseRefreshToken("not-a-jwt"))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }
}
