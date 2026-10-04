package ru.veduteam.vedu.shared.broker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SendEmailMessageTest {

  @Test
  void routesToEmailSend() {
    var message = SendEmailMessage.of("a@b.c", "otp", Map.of("code", "123456"));

    assertThat(message.routingKey()).isEqualTo("email.send");
  }

  @Test
  void generatesIdentityAndTimestamp() {
    var first = SendEmailMessage.of("a@b.c", "otp", Map.of());
    var second = SendEmailMessage.of("a@b.c", "otp", Map.of());

    assertThat(first.messageId()).isNotNull().isNotEqualTo(second.messageId());
    assertThat(first.occurredAt()).isNotNull();
  }

  @Test
  void rejectsBlankRecipient() {
    assertThatThrownBy(() -> SendEmailMessage.of(" ", "otp", Map.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsBlankTemplate() {
    assertThatThrownBy(() -> SendEmailMessage.of("a@b.c", "", Map.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
