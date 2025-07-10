package com.rinha.backend.rinhabackend2025.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public record PaymentSummary(
        @JsonIgnore String strategy,
        long totalRequests,
        BigDecimal totalAmount
) {
}
