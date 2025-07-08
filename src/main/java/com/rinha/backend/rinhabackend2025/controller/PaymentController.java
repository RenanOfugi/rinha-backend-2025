package com.rinha.backend.rinhabackend2025.controller;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> processarPagamento(@RequestBody PaymentDto paymentDto){
        service.sendPayment(paymentDto);
        return ResponseEntity.noContent().build();
    }
}
