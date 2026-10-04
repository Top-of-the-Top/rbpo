package ru.veduteam.vedu.shared.broker.internal;

import java.util.Objects;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.veduteam.vedu.shared.broker.api.BrokerClient;
import ru.veduteam.vedu.shared.broker.api.BrokerMessage;
import ru.veduteam.vedu.shared.broker.api.BrokerPublishException;

@Service
class RabbitMqClient implements BrokerClient {
  private final RabbitTemplate broker;

  RabbitMqClient(RabbitTemplate rabbitTemplate) {
    this.broker = rabbitTemplate;
  }

  @Override
  public void publish(BrokerMessage message) {
    Objects.requireNonNull(message, "message");
    try {
      broker.convertAndSend(
          BrokerTopology.EXCHANGE,
          message.routingKey(),
          message,
          amqpMessage -> {
            amqpMessage.getMessageProperties().setMessageId(message.messageId().toString());
            return amqpMessage;
          });
    } catch (AmqpException e) {
      throw new BrokerPublishException("failed to publish " + message.routingKey(), e);
    }
  }
}
