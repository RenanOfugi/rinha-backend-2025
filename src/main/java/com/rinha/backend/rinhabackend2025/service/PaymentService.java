package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import com.rinha.backend.rinhabackend2025.enums.StatusEnum;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentJob sender;


    public PaymentService(PaymentRepository repository, PaymentJob sender) {
        this.sender = sender;
        this.repository = repository;
    }

    public void sendPayment(PaymentDto paymentDto){
        repository.save(
                new Payment(null, paymentDto.getCorrelationId(), "default", paymentDto.getAmount(), LocalDateTime.now(), StatusEnum.PENDING)
        );
    }

    public List<PaymentSummary> findSummaryByPeriod(LocalDateTime from, LocalDateTime to) {
        return repository.findSummaryByPeriod(from, to);
    }
}
