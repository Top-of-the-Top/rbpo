package ru.veduteam.vedu.shared.broker.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.veduteam.vedu.shared.broker.api.BrokerPublishException;
import ru.veduteam.vedu.shared.broker.api.SendEmailMessage;

class RabbitMqClientTest {

  private final RabbitTemplate template = mock(RabbitTemplate.class);
  private final RabbitMqClient client = new RabbitMqClient(template);

  @Test
  void publishesToTopicExchangeWithMessageRoutingKey() {
    var message = SendEmailMessage.of("a@b.c", "otp", Map.of("code", "123456"));

    client.publish(message);

    verify(template).convertAndSend(
        eq(BrokerTopology.EXCHANGE), eq("email.send"), eq(message), any(MessagePostProcessor.class));
  }

  @Test
  void wrapsBrokerFailureIntoPublishException() {
    var message = SendEmailMessage.of("a@b.c", "otp", Map.of());
    doThrow(new AmqpConnectException(new RuntimeException("down")))
        .when(template)
        .convertAndSend(any(String.class), any(String.class), any(Object.class), any(MessagePostProcessor.class));

    assertThatThrownBy(() -> client.publish(message))
        .isInstanceOf(BrokerPublishException.class)
        .hasCauseInstanceOf(AmqpConnectException.class);
  }

  @Test
  void rejectsNullMessage() {
    assertThatThrownBy(() -> client.publish(null)).isInstanceOf(NullPointerException.class);
  }
}
