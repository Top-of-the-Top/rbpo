package ru.veduteam.vedu.auth.application.ports;

import java.time.Duration;
import java.util.UUID;

public interface OneTimeCodeRepository {
  public void save(String code, UUID userId, Duration ttl);

  public void consume(String code, UUID userId);

}