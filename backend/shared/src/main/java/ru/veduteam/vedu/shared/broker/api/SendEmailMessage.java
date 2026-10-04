package ru.veduteam.vedu.shared.broker.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SendEmailMessage(
    UUID messageId, Instant occurredAt, String to, String template, Map<String, String> params)
    implements BrokerMessage {

  public static final String ROUTING_KEY = "email.send";

  public SendEmailMessage {
    if (to == null || to.isBlank()) {
      throw new IllegalArgumentException("to must not be blank");
    }
    if (template == null || template.isBlank()) {
      throw new IllegalArgumentException("template must not be blank");
    }
    params = Map.copyOf(params);
  }

  public static SendEmailMessage of(String to, String template, Map<String, String> params) {
    return new SendEmailMessage(UUID.randomUUID(), Instant.now(), to, template, params);
  }

  @Override
  public String routingKey() {
    return ROUTING_KEY;
  }
}
