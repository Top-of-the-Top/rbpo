package ru.veduteam.vedu.shared.broker.api;

public interface BrokerClient {
  void publish(BrokerMessage message);
}
