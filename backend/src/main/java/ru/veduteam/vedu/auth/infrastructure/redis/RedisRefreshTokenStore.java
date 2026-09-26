package ru.veduteam.vedu.auth.infrastructure.redis;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import ru.veduteam.vedu.auth.application.ports.RefreshTokenStore;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {
  private static final String KEY_PREFIX = "refresh:";

  private final StringRedisTemplate redis;

  public RedisRefreshTokenStore(StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public void save(String tokenId, UUID userId, Duration ttl) {
    redis.opsForValue().set(KEY_PREFIX + tokenId, userId.toString(), ttl);
  }

  @Override
  public boolean consume(String tokenId) {
    return Boolean.TRUE.equals(redis.delete(KEY_PREFIX + tokenId));
  }
}
