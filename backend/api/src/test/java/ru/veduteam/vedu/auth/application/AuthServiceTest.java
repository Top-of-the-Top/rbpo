package ru.veduteam.vedu.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ru.veduteam.vedu.auth.application.dto.LoginRequest;
import ru.veduteam.vedu.auth.application.dto.RefreshTokenClaims;
import ru.veduteam.vedu.auth.application.dto.RefreshTokenRequest;
import ru.veduteam.vedu.auth.application.dto.RegisterRequest;
import ru.veduteam.vedu.auth.application.dto.TokenPair;
import ru.veduteam.vedu.auth.application.dto.UserDetails;
import ru.veduteam.vedu.auth.application.ports.Encrypter;
import ru.veduteam.vedu.auth.application.ports.PasswordHasher;
import ru.veduteam.vedu.auth.application.ports.RefreshTokenStore;
import ru.veduteam.vedu.auth.application.services.AuthService;
import ru.veduteam.vedu.auth.application.services.JwtService;
import ru.veduteam.vedu.auth.domain.errors.EmailTakenException;
import ru.veduteam.vedu.auth.domain.errors.InvalidCredentialsException;
import ru.veduteam.vedu.auth.domain.errors.InvalidRefreshTokenException;
import ru.veduteam.vedu.auth.domain.errors.UsernameTakenException;
import ru.veduteam.vedu.auth.infrastructure.crypto.SivEncrypter;
import ru.veduteam.vedu.user.application.UserRepository;
import ru.veduteam.vedu.user.domain.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordHasher passwordHasher;
  @Mock
  private RefreshTokenStore refreshTokenStore;

  private final Encrypter encrypter = new SivEncrypter(KEY, KEY);
  private final JwtService jwtService = JwtServiceTest.jwtService((byte) 1, 1_209_600_000L);

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordHasher, encrypter, jwtService, refreshTokenStore);
  }

  private static User userWithId(String username, String passwordHash) {
    User user = new User(username, "encrypted", passwordHash);
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  @Test
  void registerSavesUserWithEncryptedNormalizedEmailAndHashedPassword() {
    when(passwordHasher.hash("password123")).thenReturn("bcrypt-hash");
    when(userRepository.saveAndFlush(any(User.class))).thenAnswer(call -> {
      User saved = call.getArgument(0);
      ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
      return saved;
    });

    TokenPair tokens = authService.register(new RegisterRequest("bob", "  Bob@Example.COM ", "password123"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).saveAndFlush(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getUsername()).isEqualTo("bob");
    assertThat(saved.getPasswordHash()).isEqualTo("bcrypt-hash");
    assertThat(encrypter.decrypt(saved.getEncryptedEmail())).isEqualTo("bob@example.com");

    RefreshTokenClaims claims = jwtService.parseRefreshToken(tokens.refreshToken());
    assertThat(claims.userId()).isEqualTo(saved.getId());
    verify(refreshTokenStore).save(claims.tokenId(), saved.getId(), Duration.ofDays(14));
    assertThat(tokens.accessToken()).isNotBlank();
  }

  @Test
  void registerRejectsTakenUsername() {
    when(userRepository.existsByUsername("bob")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("bob", "bob@example.com", "password123")))
        .isInstanceOf(UsernameTakenException.class);
    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void registerRejectsTakenEmailRegardlessOfCase() {
    when(userRepository.existsByEncryptedEmail(encrypter.encrypt("bob@example.com"))).thenReturn(true);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("bob", "BOB@example.com", "password123")))
        .isInstanceOf(EmailTakenException.class);
    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void loginIssuesTokensForCorrectPassword() {
    User user = userWithId("bob", "bcrypt-hash");
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
    when(passwordHasher.matches("password123", "bcrypt-hash")).thenReturn(true);

    TokenPair tokens = authService.login(new LoginRequest("bob", "password123"));

    RefreshTokenClaims claims = jwtService.parseRefreshToken(tokens.refreshToken());
    assertThat(claims.userId()).isEqualTo(user.getId());
    verify(refreshTokenStore).save(claims.tokenId(), user.getId(), Duration.ofDays(14));
  }

  @Test
  void loginRejectsWrongPassword() {
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(userWithId("bob", "bcrypt-hash")));
    when(passwordHasher.matches("wrong", "bcrypt-hash")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("bob", "wrong")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void loginRejectsUnknownUserWithSameError() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "password123")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void refreshConsumesOldTokenAndIssuesNewPair() {
    User user = userWithId("bob", "bcrypt-hash");
    String oldToken = jwtService.generateRefreshToken(new UserDetails(user.getId(), "bob"), "old-id");
    when(refreshTokenStore.consume("old-id")).thenReturn(true);
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    TokenPair tokens = authService.refresh(new RefreshTokenRequest(oldToken));

    String newTokenId = jwtService.parseRefreshToken(tokens.refreshToken()).tokenId();
    assertThat(newTokenId).isNotEqualTo("old-id");
    verify(refreshTokenStore).save(newTokenId, user.getId(), Duration.ofDays(14));
  }

  @Test
  void refreshRejectsAlreadyUsedToken() {
    String usedToken = jwtService.generateRefreshToken(new UserDetails(UUID.randomUUID(), "bob"), "used-id");
    when(refreshTokenStore.consume("used-id")).thenReturn(false);

    assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(usedToken)))
        .isInstanceOf(InvalidRefreshTokenException.class);
    verify(refreshTokenStore, never()).save(anyString(), any(), any());
  }

  @Test
  void refreshRejectsTokenOfDeletedUser() {
    UUID deletedUserId = UUID.randomUUID();
    String token = jwtService.generateRefreshToken(new UserDetails(deletedUserId, "bob"), "token-id");
    when(refreshTokenStore.consume("token-id")).thenReturn(true);
    when(userRepository.findById(deletedUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(token)))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void logoutConsumesRefreshToken() {
    String token = jwtService.generateRefreshToken(new UserDetails(UUID.randomUUID(), "bob"), "token-id");

    authService.logout(new RefreshTokenRequest(token));

    verify(refreshTokenStore).consume(eq("token-id"));
  }

  @Test
  void logoutWithInvalidTokenSucceedsSilently() {
    assertThatCode(() -> authService.logout(new RefreshTokenRequest("not-a-jwt"))).doesNotThrowAnyException();
    verify(refreshTokenStore, never()).consume(anyString());
  }
}
