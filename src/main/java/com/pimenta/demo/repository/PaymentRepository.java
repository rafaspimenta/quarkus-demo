package com.pimenta.demo.repository;

import com.pimenta.demo.model.Payment;
import com.pimenta.demo.dto.PaymentSummary;

import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PaymentRepository {

    DataSource dataSource;

    public PaymentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Payment payment) throws SQLException {
        String sql = """
                INSERT INTO payments (id, correlation_id, amount, requested_at, processor_type, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, payment.getCorrelationId());
            statement.setBigDecimal(3, payment.getAmount());
            statement.setTimestamp(4, Timestamp.from(payment.getRequestedAt()));
            statement.setString(5, payment.getProcessorType().name());
            statement.setTimestamp(6, Timestamp.from(payment.getCreatedAt()));

            statement.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM payments";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
        }
    }

    public PaymentSummary getPaymentSummary(Instant fromDate, Instant toDate) throws SQLException {
        StringBuilder query = new StringBuilder("""
                SELECT
                    COUNT(*) FILTER (WHERE processor_type = 'DEFAULT') as default_total_requests,
                    COALESCE(SUM(amount) FILTER (WHERE processor_type = 'DEFAULT'), 0) as default_total_amount,
                    COUNT(*) FILTER (WHERE processor_type = 'FALLBACK') as fallback_total_requests,
                    COALESCE(SUM(amount) FILTER (WHERE processor_type = 'FALLBACK'), 0) as fallback_total_amount
                FROM payments
                """);

        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            query.append(" WHERE requested_at >= ?");
            params.add(Timestamp.from(fromDate));
        }

        if (toDate != null) {
            query.append(fromDate != null ? " AND" : " WHERE").append(" requested_at <= ?");
            params.add(Timestamp.from(toDate));
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long defaultTotalRequests = resultSet.getLong("default_total_requests");
                    BigDecimal defaultTotalAmount = resultSet.getBigDecimal("default_total_amount");
                    long fallbackTotalRequests = resultSet.getLong("fallback_total_requests");
                    BigDecimal fallbackTotalAmount = resultSet.getBigDecimal("fallback_total_amount");

                    return PaymentSummary.builder()
                            .defaultProcessor(PaymentSummary.ProcessorSummary.builder()
                                    .totalRequests(defaultTotalRequests)
                                    .totalAmount(defaultTotalAmount)
                                    .build())
                            .fallback(PaymentSummary.ProcessorSummary.builder()
                                    .totalRequests(fallbackTotalRequests)
                                    .totalAmount(fallbackTotalAmount)
                                    .build())
                            .build();
                }
            }
        }
        return null;
    }
}