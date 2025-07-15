package com.rinha.backend.rinhabackend2025.entity;

import com.rinha.backend.rinhabackend2025.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID correlationId;

    private String strategy;

    private BigDecimal amount;

    private LocalDateTime timestamp;

    private StatusEnum status;
}
