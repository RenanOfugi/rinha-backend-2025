package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import com.rinha.backend.rinhabackend2025.service.producer.RabbitMQProducer;
import com.rinha.backend.rinhabackend2025.utils.ConstantUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final RabbitMQProducer<PaymentDto> producer;

    private final PaymentRepository repository;

    public PaymentService(RabbitMQProducer<PaymentDto> producer, PaymentRepository repository) {
        this.producer = producer;
        this.repository = repository;
    }

    public void sendPayment(PaymentDto paymentDto){
        producer.enviarMensagem(
                ConstantUtils.RabbitMQ.PAYMENT_EXCHANGE,
                ConstantUtils.RabbitMQ.PAYMENT_ROUTING_KEY,
                paymentDto
        );
    }

    public List<PaymentSummary> findSummaryByPeriod(LocalDateTime from, LocalDateTime to) {
        return repository.findSummaryByPeriod(from, to);
    }
}
