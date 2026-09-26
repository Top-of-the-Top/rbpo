package ru.veduteam.vedu.auth.application.services;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.veduteam.vedu.auth.application.dto.LoginRequest;
import ru.veduteam.vedu.auth.application.dto.RefreshTokenClaims;
import ru.veduteam.vedu.auth.application.dto.RefreshTokenRequest;
import ru.veduteam.vedu.auth.application.dto.RegisterRequest;
import ru.veduteam.vedu.auth.application.dto.TokenPair;
import ru.veduteam.vedu.auth.application.dto.UserDetails;
import ru.veduteam.vedu.auth.application.ports.Encrypter;
import ru.veduteam.vedu.auth.application.ports.PasswordHasher;
import ru.veduteam.vedu.auth.application.ports.RefreshTokenStore;
import ru.veduteam.vedu.auth.domain.errors.EmailTakenException;
import ru.veduteam.vedu.auth.domain.errors.InvalidCredentialsException;
import ru.veduteam.vedu.auth.domain.errors.InvalidRefreshTokenException;
import ru.veduteam.vedu.auth.domain.errors.UsernameTakenException;
import ru.veduteam.vedu.user.application.UserRepository;
import ru.veduteam.vedu.user.domain.User;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final Encrypter encrypter;
  private final JwtService jwtService;
  private final RefreshTokenStore refreshTokenStore;

  public AuthService(UserRepository userRepository, PasswordHasher passwordHasher, Encrypter encrypter,
      JwtService jwtService, RefreshTokenStore refreshTokenStore) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.encrypter = encrypter;
    this.jwtService = jwtService;
    this.refreshTokenStore = refreshTokenStore;
  }

  @Transactional
  public TokenPair register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new UsernameTakenException();
    }

    String encryptedEmail = encrypter.encrypt(normalizeEmail(request.email()));
    if (userRepository.existsByEncryptedEmail(encryptedEmail)) {
      throw new EmailTakenException();
    }

    User user = new User(request.username(), encryptedEmail, passwordHasher.hash(request.password()));
    return issueTokens(userRepository.saveAndFlush(user));
  }

  public TokenPair login(LoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .filter(found -> passwordHasher.matches(request.password(), found.getPasswordHash()))
        .orElseThrow(InvalidCredentialsException::new);
    return issueTokens(user);
  }

  public TokenPair refresh(RefreshTokenRequest request) {
    RefreshTokenClaims claims = jwtService.parseRefreshToken(request.refreshToken());
    if (!refreshTokenStore.consume(claims.tokenId())) {
      throw new InvalidRefreshTokenException();
    }

    User user = userRepository.findById(claims.userId()).orElseThrow(InvalidRefreshTokenException::new);
    return issueTokens(user);
  }

  public void logout(RefreshTokenRequest request) {
    try {
      refreshTokenStore.consume(jwtService.parseRefreshToken(request.refreshToken()).tokenId());
    } catch (InvalidRefreshTokenException _) {
    }
  }

  private TokenPair issueTokens(User user) {
    UserDetails userDetails = new UserDetails(user.getId(), user.getUsername());
    String tokenId = UUID.randomUUID().toString();

    String refreshToken = jwtService.generateRefreshToken(userDetails, tokenId);
    refreshTokenStore.save(tokenId, user.getId(), jwtService.getRefreshTokenTtl());

    return new TokenPair(jwtService.generateAccessToken(userDetails), refreshToken);
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
