package com.pimenta.demo.service;

import com.pimenta.demo.client.PaymentProcessorClient;
import com.pimenta.demo.client.PaymentProcessorFallbackClient;
import com.pimenta.demo.dto.PaymentRequest;
import com.pimenta.demo.dto.PaymentSummary;
import com.pimenta.demo.dto.ProcessorPaymentRequest;
import com.pimenta.demo.exception.PaymentDatabaseException;
import com.pimenta.demo.model.Payment;
import com.pimenta.demo.model.ProcessorType;
import com.pimenta.demo.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class PaymentService2 {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessorClient paymentProcessorClient;
    private final PaymentProcessorFallbackClient fallbackClient;

    public PaymentService2(PaymentRepository paymentRepository,
                           @RestClient PaymentProcessorClient paymentProcessorClient,
                           @RestClient PaymentProcessorFallbackClient fallbackClient) {
        this.paymentRepository = paymentRepository;
        this.paymentProcessorClient = paymentProcessorClient;
        this.fallbackClient = fallbackClient;
    }

    /*public void process(PaymentRequest paymentRequest) {
        Instant requestedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        // Create request for external payment processor
        ProcessorPaymentRequest processorRequest = ProcessorPaymentRequest.builder()
                .correlationId(paymentRequest.getCorrelationId())
                .amount(paymentRequest.getAmount())
                .requestedAt(requestedAt)
                .build();

        Response response;
        ProcessorType processorType = ProcessorType.DEFAULT;

        try {
            log.info("Attempting payment with primary processor for correlation ID: {}",
                    paymentRequest.getCorrelationId());
            response = paymentProcessorClient.processPayment(processorRequest);
        } catch (Exception e) {
            log.warn("Primary processor failed, attempting fallback for correlation ID: {}",
                    paymentRequest.getCorrelationId());
            try {
                response = fallbackClient.processPayment(processorRequest);
                processorType = ProcessorType.FALLBACK;
            } catch (Exception fallbackEx) {
                log.error("Fallback processor also failed for correlation ID: {}", paymentRequest.getCorrelationId());
                throw fallbackEx;
            }
        }

        if (response == null || response.getStatus() != 200) {
            throw new WebApplicationException("Payment processing failed",
                    response != null ? response.getStatus() : 500);
        }

        // If processing was successful, save to the database
        var payment = Payment.builder()
                .id(UUID.randomUUID())
                .correlationId(paymentRequest.getCorrelationId())
                .amount(paymentRequest.getAmount())
                .requestedAt(requestedAt)
                .processorType(processorType)
                .createdAt(Instant.now())
                .build();

        try {
            paymentRepository.save(payment);
        } catch (SQLException e) {
            throw new PaymentDatabaseException("Error saving payment to database", e);
        } finally {
            response.close();
        }
    }*/

    public void deleteAllPayments() throws SQLException {
        paymentRepository.deleteAll();
    }

    public PaymentSummary getPaymentSummary(Instant fromDate, Instant toDate) throws SQLException {
        return paymentRepository.getPaymentSummary(fromDate, toDate);
    }
}