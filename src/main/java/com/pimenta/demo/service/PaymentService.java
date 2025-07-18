package com.pimenta.demo.service;

import com.pimenta.demo.client.PaymentProcessorClient;
import com.pimenta.demo.client.PaymentProcessorFallbackClient;
import com.pimenta.demo.dto.*;
import com.pimenta.demo.exception.PaymentDatabaseException;
import com.pimenta.demo.exception.ProcessorUnavailableException;
import com.pimenta.demo.model.Payment;
import com.pimenta.demo.model.ProcessorType;
import com.pimenta.demo.repository.PaymentRepository;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@ApplicationScoped
public class PaymentService {

    private final AtomicBoolean defaultProcessorHealthy = new AtomicBoolean(true);
    private final AtomicBoolean fallbackProcessorHealthy = new AtomicBoolean(true);

    private final PaymentRepository paymentRepository;
    private final PaymentProcessorClient paymentProcessorClientDefault;
    private final PaymentProcessorFallbackClient paymentProcessorFallbackClient;

    public PaymentService(PaymentRepository paymentRepository,
            @RestClient PaymentProcessorClient paymentProcessorClientDefault,
            @RestClient PaymentProcessorFallbackClient paymentProcessorFallbackClient) {
        this.paymentRepository = paymentRepository;
        this.paymentProcessorClientDefault = paymentProcessorClientDefault;
        this.paymentProcessorFallbackClient = paymentProcessorFallbackClient;
    }

    public void process(PaymentRequest request) {
        Instant requestedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        try {
            validateProcessorsHealth(request.getCorrelationId());
            ProcessorPaymentResponse response = processWithAvailableProcessor(request, requestedAt);
            savePayment(request, response, requestedAt);
        } catch (Exception e) {
            log.error("Error processing payment for correlationId: {}", request.getCorrelationId(), e);
            throw e;
        }
    }

    private void validateProcessorsHealth(UUID correlationId) {
        if (!defaultProcessorHealthy.get() && !fallbackProcessorHealthy.get()) {
            throwProcessorsUnavailable(correlationId);
        }
    }

    private void throwProcessorsUnavailable(UUID correlationId) {
        log.error("All processors are unhealthy, failing fast for: {}", correlationId);
        throw new ProcessorUnavailableException("All payment processors are currently unavailable");
    }

    private ProcessorPaymentResponse processWithAvailableProcessor(PaymentRequest request, Instant requestedAt) {
        return defaultProcessorHealthy.get()
                ? tryDefaultWithFallback(request, requestedAt)
                : tryFallbackOnly(request, requestedAt);
    }

    private ProcessorPaymentResponse tryDefaultWithFallback(PaymentRequest request, Instant requestedAt) {
        try {
            return processPayment(request, ProcessorType.DEFAULT, requestedAt);
        } catch (Exception e) {
            log.error("Default processor failed, marking as unhealthy and trying fallback for: {}",
                    request.getCorrelationId(), e);
            defaultProcessorHealthy.set(false);
            return handleFallbackProcessing(request, requestedAt);
        }
    }

    private ProcessorPaymentResponse tryFallbackOnly(PaymentRequest request, Instant requestedAt) {
        if (!fallbackProcessorHealthy.get()) {
            throwProcessorsUnavailable(request.getCorrelationId());
        }

        log.debug("Default processor is unhealthy, using fallback directly for: {}", request.getCorrelationId());
        return handleFallbackProcessing(request, requestedAt);
    }

    private ProcessorPaymentResponse processPayment(PaymentRequest paymentRequest,
            ProcessorType processorType,
            Instant requestedAt) {
        ProcessorPaymentRequest processorRequest = ProcessorPaymentRequest.builder()
                .correlationId(paymentRequest.getCorrelationId())
                .amount(paymentRequest.getAmount())
                .requestedAt(requestedAt)
                .build();

        var processorResponse = processorType == ProcessorType.DEFAULT
                ? paymentProcessorClientDefault.processPayment(processorRequest)
                : paymentProcessorFallbackClient.processPayment(processorRequest);

        if (processorResponse != null) {
            processorResponse.setProcessorType(processorType);
        }

        return processorResponse;
    }

    private ProcessorPaymentResponse handleFallbackProcessing(PaymentRequest request, Instant requestedAt) {
        if (!fallbackProcessorHealthy.get()) {
            log.error("Fallback processor is unhealthy for: {}", request.getCorrelationId());
            throw new ProcessorUnavailableException("All payment processors are currently unavailable");
        }

        try {
            return processPayment(request, ProcessorType.FALLBACK, requestedAt);
        } catch (Exception e) {
            log.error("Fallback processor failed, marking as unhealthy for: {}", request.getCorrelationId());
            fallbackProcessorHealthy.set(false);
            throw e;
        }
    }

    private void savePayment(PaymentRequest request, ProcessorPaymentResponse response, Instant requestedAt) {
        if (response != null) {
            Payment payment = Payment.builder()
                    .correlationId(request.getCorrelationId())
                    .amount(request.getAmount())
                    .requestedAt(requestedAt)
                    .processorType(response.getProcessorType())
                    .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                    .build();

            try {
                paymentRepository.save(payment);
            } catch (SQLException e) {
                throw new PaymentDatabaseException("Error saving payment to database", e);
            }

            log.debug("Payment saved successfully: {}, processor: {}",
                    request.getCorrelationId(),
                    response.getProcessorType());
        }
    }

    @Scheduled(every = "1s")
    public void checkPaymentProcessorHealth() {
        // Only check unhealthy processors to reduce unnecessary calls
        if (!defaultProcessorHealthy.get()) {
            checkProcessorHealth(ProcessorType.DEFAULT, defaultProcessorHealthy);
        }
        if (!fallbackProcessorHealthy.get()) {
            checkProcessorHealth(ProcessorType.FALLBACK, fallbackProcessorHealthy);
        }
    }

    private void checkProcessorHealth(ProcessorType processorType, AtomicBoolean healthState) {
        try {
            ProcessorHealthResponse healthResponse = processorType == ProcessorType.DEFAULT
                    ? paymentProcessorClientDefault.health()
                    : paymentProcessorFallbackClient.health();

            if (!healthResponse.isFailing()) {
                healthState.set(true);
                log.info("{} processor recovered, marked as healthy", processorType);
            }
        } catch (Exception e) {
            log.warn("Health check failed for {} processor, keeping as unhealthy", processorType);
        }
    }

    public void deleteAllPayments() throws SQLException {
        paymentRepository.deleteAll();
    }

    public PaymentSummary getPaymentSummary(Instant fromDate, Instant toDate) throws SQLException {
        return paymentRepository.getPaymentSummary(fromDate, toDate);
    }
}