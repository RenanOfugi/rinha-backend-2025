package com.rinha.backend.rinhabackend2025.service.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQProducer<T> {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarMensagem(String exchange, String routingKey, T objeto) {
        rabbitTemplate.convertAndSend(exchange, routingKey, objeto);
    }
}