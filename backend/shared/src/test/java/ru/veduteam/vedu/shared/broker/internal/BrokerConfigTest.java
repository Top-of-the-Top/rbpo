package ru.veduteam.vedu.shared.broker.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;
import ru.veduteam.vedu.shared.broker.api.SendEmailMessage;

class BrokerConfigTest {

  private final BrokerConfig config = new BrokerConfig();

  @Test
  void exchangeIsDurableTopic() {
    var exchange = config.brokerExchange();

    assertThat(exchange.getName()).isEqualTo("vedu.events");
    assertThat(exchange.getType()).isEqualTo("topic");
    assertThat(exchange.isDurable()).isTrue();
  }

  @Test
  void emailQueueIsDurableAndBoundToEmailKeys() {
    var queue = config.emailQueue();
    var binding = config.emailBinding(queue, config.brokerExchange());

    assertThat(queue.isDurable()).isTrue();
    assertThat(binding.getRoutingKey()).isEqualTo("email.#");
    assertThat(binding.getDestination()).isEqualTo(queue.getName());
  }

  @Test
  void messageSurvivesJsonRoundTrip() {
    var converter = config.messageConverter();
    var original = SendEmailMessage.of("a@b.c", "otp", Map.of("code", "123456"));

    var restored = converter.fromMessage(converter.toMessage(original, new MessageProperties()));

    assertThat(restored).isEqualTo(original);
  }
}
