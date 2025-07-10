package com.rinha.backend.rinhabackend2025.config;

import com.rinha.backend.rinhabackend2025.utils.ConstantUtils;
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
        return new Queue(ConstantUtils.RabbitMQ.PAYMENT_QUEUE);
    }

    @Bean
    public DirectExchange paymentExchange(){
        return new DirectExchange(ConstantUtils.RabbitMQ.PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding paymentBinding(){
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange())
                .with(ConstantUtils.RabbitMQ.PAYMENT_ROUTING_KEY);
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
