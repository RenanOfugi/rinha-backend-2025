package com.rinha.backend.rinhabackend2025.config;

import com.rinha.backend.rinhabackend2025.utils.RabbitEnum;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue paymentQueue(){
        return new Queue(RabbitEnum.PAYMENT_QUEUE.getValue());
    }

    @Bean
    public DirectExchange paymentExchange(){
        return new DirectExchange(RabbitEnum.PAYMENT_EXCHANGE.getValue());
    }

    @Bean
    public Binding paymentBinding(){
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange())
                .with(RabbitEnum.PAYMENT_ROUTING_KEY.getValue());
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter());
        return template;
    }
}
