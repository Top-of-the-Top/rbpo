package ru.veduteam.vedu.shared.broker.internal;

final class BrokerTopology {
  static final String EXCHANGE = "vedu.events";
  static final String EMAIL_QUEUE = "vedu.email";
  static final String EMAIL_BINDING_KEY = "email.#";

  private BrokerTopology() {}
}
