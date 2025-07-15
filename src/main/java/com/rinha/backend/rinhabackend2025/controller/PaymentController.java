package com.rinha.backend.rinhabackend2025.controller;

import com.rinha.backend.rinhabackend2025.dto.PaymentDto;
import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
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

    @GetMapping("/payments-summary")
    public ResponseEntity<Map<String, PaymentSummary>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {

        List<PaymentSummary> summaries = service.findSummaryByPeriod(from, to);

        Map<String, PaymentSummary> response = summaries.stream()
                .collect(Collectors.toMap(PaymentSummary::getStrategy, Function.identity()));

        response.putIfAbsent("default", new PaymentSummary("default", 0L, BigDecimal.ZERO));
        response.putIfAbsent("fallback", new PaymentSummary("fallback", 0L, BigDecimal.ZERO));

        return ResponseEntity.ok(response);
    }
}
