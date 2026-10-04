package ru.veduteam.vedu.shared.broker.internal;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BrokerConfig {
  private static final String CONTRACT_PACKAGE = "ru.veduteam.vedu.shared.broker.api";

  @Bean
  TopicExchange brokerExchange() {
    return new TopicExchange(BrokerTopology.EXCHANGE, true, false);
  }

  @Bean
  Queue emailQueue() {
    return QueueBuilder.durable(BrokerTopology.EMAIL_QUEUE).build();
  }

  @Bean
  Binding emailBinding(Queue emailQueue, TopicExchange brokerExchange) {
    return BindingBuilder.bind(emailQueue).to(brokerExchange).with(BrokerTopology.EMAIL_BINDING_KEY);
  }

  @Bean
  JacksonJsonMessageConverter messageConverter() {
    var converter = new JacksonJsonMessageConverter();
    ((DefaultJacksonJavaTypeMapper) converter.getJavaTypeMapper()).setTrustedPackages(CONTRACT_PACKAGE);
    return converter;
  }
}
