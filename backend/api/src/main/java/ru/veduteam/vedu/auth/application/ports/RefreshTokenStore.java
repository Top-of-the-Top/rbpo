package ru.veduteam.vedu.auth.application.ports;

import java.time.Duration;
import java.util.UUID;

public interface RefreshTokenStore {
  void save(String tokenId, UUID userId, Duration ttl);

  boolean consume(String tokenId);
}
