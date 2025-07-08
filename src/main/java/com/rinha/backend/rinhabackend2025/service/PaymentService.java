package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.utils.RabbitEnum;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final RabbitMQProducer<PaymentDto> producer;

    public PaymentService(RabbitMQProducer<PaymentDto> producer) {
        this.producer = producer;
    }

    public void sendPayment(PaymentDto paymentDto){
        producer.enviarMensagem(
                RabbitEnum.PAYMENT_EXCHANGE.getValue(),
                RabbitEnum.PAYMENT_ROUTING_KEY.getValue(),
                paymentDto
        );
    }
}
