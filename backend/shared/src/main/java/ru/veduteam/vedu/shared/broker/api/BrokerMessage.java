package ru.veduteam.vedu.shared.broker.api;

import java.time.Instant;
import java.util.UUID;

public interface BrokerMessage {
  UUID messageId();

  Instant occurredAt();

  String routingKey();
}
