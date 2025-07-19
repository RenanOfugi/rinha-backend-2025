package com.rinha.backend.rinhabackend2025.service;

import com.rinha.backend.rinhabackend2025.entity.Payment;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.sql.*;
import java.util.List;

@AllArgsConstructor
public class JdbcService {

    private String url;
    private String username;
    private String password;

    @Transactional
    public void saveAll(List<Payment> payments){

        try(Connection conn = DriverManager.getConnection(url, username, password)) {

            String sql = "INSERT INTO payment (correlation_id, strategy, amount, timestamp, status) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            conn.setAutoCommit(false);

            for (Payment payment : payments) {
                stmt.setString(1, String.valueOf(payment.getCorrelationId()));
                stmt.setString(2, payment.getStrategy());
                stmt.setBigDecimal(3, payment.getAmount());
                stmt.setTimestamp(4, Timestamp.from(payment.getTimestamp()));
                stmt.setString(5, String.valueOf(payment.getStatus()));
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar dados");
        }
    }
}
