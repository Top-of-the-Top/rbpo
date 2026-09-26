package ru.veduteam.vedu.auth.application.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.veduteam.vedu.auth.application.dto.RefreshTokenClaims;
import ru.veduteam.vedu.auth.application.dto.UserDetails;
import ru.veduteam.vedu.auth.domain.errors.InvalidRefreshTokenException;

@Service
public class JwtService {
  private static final String TOKEN_TYPE_CLAIM = "type";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.access-token.expiration}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token.expiration}")
  private long refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }

  public String generateAccessToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails, accessTokenExpiration, ACCESS_TOKEN_TYPE);
  }

  public String generateRefreshToken(UserDetails userDetails, String tokenId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(Claims.ID, tokenId);
    return generateToken(claims, userDetails, refreshTokenExpiration, REFRESH_TOKEN_TYPE);
  }

  public Duration getRefreshTokenTtl() {
    return Duration.ofMillis(refreshTokenExpiration);
  }

  public RefreshTokenClaims parseRefreshToken(String token) {
    Claims claims;
    try {
      claims = Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException | IllegalArgumentException _) {
      throw new InvalidRefreshTokenException();
    }

    if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class)) || claims.getId() == null) {
      throw new InvalidRefreshTokenException();
    }
    return new RefreshTokenClaims(UUID.fromString(claims.getSubject()), claims.getId());
  }

  private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration,
      String tokenType) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.id().toString())
        .claim("username", userDetails.username())
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }
}
