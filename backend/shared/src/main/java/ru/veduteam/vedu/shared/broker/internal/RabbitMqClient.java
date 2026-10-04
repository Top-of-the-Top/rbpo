package ru.veduteam.vedu.shared.broker.internal;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.veduteam.vedu.shared.broker.api.BrokerClient;

@Service
public class RabbitMqClient implements BrokerClient {
  private final RabbitTemplate broker;

  public RabbitMqClient(RabbitTemplate rabbitTemplate) {
    this.broker = rabbitTemplate;
  }

  @Override
  public void publish() {
    // TODO:
  }
}
