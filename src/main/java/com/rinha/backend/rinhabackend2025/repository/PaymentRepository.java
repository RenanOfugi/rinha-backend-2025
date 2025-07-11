package com.rinha.backend.rinhabackend2025.repository;

import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
        SELECT new com.rinha.backend.rinhabackend2025.dto.PaymentSummary(
            pl.strategy,
            COUNT(pl),
            COALESCE(SUM(pl.amount), 0)
        )
        FROM Payment pl
        WHERE pl.timestamp BETWEEN :from AND :to
        GROUP BY pl.strategy
    """)
    List<PaymentSummary> findSummaryByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
