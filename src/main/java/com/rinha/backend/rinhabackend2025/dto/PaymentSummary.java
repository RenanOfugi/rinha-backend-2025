package com.rinha.backend.rinhabackend2025.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public final class PaymentSummary {
    @JsonIgnore
    private String strategy;
    private Long totalRequests;
    private BigDecimal totalAmount;
}
