package com.rinha.backend.rinhabackend2025.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rinha.backend.rinhabackend2025.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_timestamp_status", columnList = "timestamp,status"),
        @Index(name = "idx_payment_strategy", columnList = "strategy"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_correlation_id", columnList = "correlationId", unique = true),
        @Index(name = "idx_payment_timestamp", columnList = "timestamp")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {

    @Id
    private UUID correlationId;

    private String strategy;

    private BigDecimal amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;
}
