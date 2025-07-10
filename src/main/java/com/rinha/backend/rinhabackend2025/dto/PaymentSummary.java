package com.rinha.backend.rinhabackend2025.dto;

import java.math.BigDecimal;

public record PaymentSummary(
        String strategy,
        long totalRequests,
        BigDecimal totalAmount
) {
}
