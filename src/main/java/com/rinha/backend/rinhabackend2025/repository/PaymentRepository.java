package com.rinha.backend.rinhabackend2025.repository;

import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
        SELECT new com.rinha.backend.rinhabackend2025.dto.PaymentSummary(
            pl.strategy,
            COUNT(pl),
            COALESCE(SUM(pl.amount), 0)
        )
        FROM Payment pl
        WHERE :from <= pl.timestamp AND pl.timestamp <= :to
        AND pl.status = com.rinha.backend.rinhabackend2025.enums.StatusEnum.OK
        GROUP BY pl.strategy
    """)
    List<PaymentSummary> findSummaryByPeriod(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
                SELECT p.* FROM payment p WHERE p.status = 'PENDING'
                LIMIT 50
                FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Payment> findPendingPayment();
}
