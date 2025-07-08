package com.rinha.backend.rinhabackend2025.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RabbitEnum {

    PAYMENT_QUEUE("payment-queue"),
    PAYMENT_EXCHANGE("payment-exchange"),
    PAYMENT_ROUTING_KEY("payment-routing-key");

    private final String value;
}
