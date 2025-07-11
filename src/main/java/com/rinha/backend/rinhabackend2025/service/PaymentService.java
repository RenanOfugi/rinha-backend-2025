package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentSender sender;


    public PaymentService(PaymentRepository repository, PaymentSender sender) {
        this.sender = sender;
        this.repository = repository;
    }

    public void sendPayment(PaymentDto paymentDto){
        paymentDto.setRequestedAt(LocalDateTime.now());
        sender.sendPayment(paymentDto);
    }

    public List<PaymentSummary> findSummaryByPeriod(LocalDateTime from, LocalDateTime to) {
        return repository.findSummaryByPeriod(from, to);
    }
}
