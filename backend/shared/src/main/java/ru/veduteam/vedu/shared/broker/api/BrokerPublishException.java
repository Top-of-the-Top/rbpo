package ru.veduteam.vedu.shared.broker.api;

public class BrokerPublishException extends RuntimeException {
  public BrokerPublishException(String message, Throwable cause) {
    super(message, cause);
  }
}
