package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.service.producer.RabbitMQProducer;
import com.rinha.backend.rinhabackend2025.utils.ConstantUtils;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RabbitMQProducer<PaymentDto> producer;

    public PaymentService(RabbitMQProducer<PaymentDto> producer) {
        this.producer = producer;
    }

    public void sendPayment(PaymentDto paymentDto){
        producer.enviarMensagem(
                ConstantUtils.RabbitMQ.PAYMENT_EXCHANGE,
                ConstantUtils.RabbitMQ.PAYMENT_ROUTING_KEY,
                paymentDto
        );
    }
}
