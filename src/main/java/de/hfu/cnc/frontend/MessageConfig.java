package de.hfu.cnc.frontend;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {
    @Bean
    public DirectExchange direct() {
        return new DirectExchange("cnc.direct");
    }

    @Bean
    public Queue queue(@Value("${service.routingKey}") String routingKey) {
        return new Queue("cnc." + routingKey);
    }

    @Bean
    public Binding binding1(DirectExchange direct, Queue queue, @Value("${service.routingKey}") String routingKey) {
        return BindingBuilder.bind(queue).to(direct).with(routingKey);
    }
}
