package com.rinha.backend.rinhabackend2025.repository;

import com.rinha.backend.rinhabackend2025.dto.PaymentSummary;
import com.rinha.backend.rinhabackend2025.entity.Payment;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.LockOptions;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
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
        AND pl.status = 'OK'
        GROUP BY pl.strategy
    """)
    List<PaymentSummary> findSummaryByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
                SELECT p.* FROM payment p WHERE p.status = 'PENDING'
                LIMIT 100
                FOR UPDATE SKIP LOCKED
            """)
    List<Payment> findPendingPayment();
}
