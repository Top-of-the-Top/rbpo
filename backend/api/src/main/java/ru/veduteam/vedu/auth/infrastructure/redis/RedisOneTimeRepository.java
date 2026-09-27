package ru.veduteam.vedu.auth.infrastructure.redis;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import java.util.Collections;

import ru.veduteam.vedu.auth.application.ports.OneTimeCodeRepository;

@Repository
public class RedisOneTimeRepository implements OneTimeCodeRepository {
  private static final String KEY_PREFIX = "otp:";
  private final StringRedisTemplate redis;

  private static final DefaultRedisScript<Long> DELETE_IF_MATCH_SCRIPT;

  static {
    DELETE_IF_MATCH_SCRIPT = new DefaultRedisScript<>();
    DELETE_IF_MATCH_SCRIPT.setScriptText(
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end");
    DELETE_IF_MATCH_SCRIPT.setResultType(Long.class);
  } // Вот этот скрипт Lua нужен для атомарной транзакции в Redis - иначе надо 2
    // неатмарных операции делать, что не очень хорошо

  public RedisOneTimeRepository(StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public void save(String code, UUID userId, Duration ttl) {
    redis.opsForValue().set(KEY_PREFIX + userId, code, ttl);
  }

  @Override
  public boolean verify(String code, UUID userId) {
    String key = KEY_PREFIX + userId;

    Long result = redis.execute(
        DELETE_IF_MATCH_SCRIPT,
        Collections.singletonList(key),
        code);

    return result != null && result == 1L;

  }
}
