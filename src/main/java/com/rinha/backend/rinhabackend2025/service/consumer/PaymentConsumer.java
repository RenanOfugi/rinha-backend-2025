package com.rinha.backend.rinhabackend2025.service.consumer;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.service.PaymentSender;
import com.rinha.backend.rinhabackend2025.utils.ConstantUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentConsumer {

    private final PaymentSender paymentSender;

    public PaymentConsumer(PaymentSender paymentSender) {
        this.paymentSender = paymentSender;
    }

    @RabbitListener(queues = ConstantUtils.RabbitMQ.PAYMENT_QUEUE)
    public void sendPayment(PaymentDto paymentDto){
        paymentDto.setRequestedAt(LocalDateTime.now());
        paymentSender.sendPayment(paymentDto);
    }

}
