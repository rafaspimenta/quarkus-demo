package com.pimenta.demo.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pimenta.demo.dto.PaymentRequest;
import com.pimenta.demo.service.PaymentService;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class PaymentQueueService {

    private final PaymentService paymentService;
    private final Emitter<PaymentRequest> paymentEmitter;
    private final ObjectMapper objectMapper;

    @Inject
    public PaymentQueueService(PaymentService paymentService,
            @Channel("payments") Emitter<PaymentRequest> paymentEmitter,
            ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.paymentEmitter = paymentEmitter;
        this.objectMapper = objectMapper;
    }

    public void enqueue(PaymentRequest payment) {
        log.info("Enqueueing payment to RabbitMQ");
        paymentEmitter.send(payment);
    }

    @Incoming("process-payments")
    @Blocking
    void processPayment(JsonObject json) throws JsonProcessingException {
        log.info("Processing payment from RabbitMQ queue");

        try {
            PaymentRequest paymentRequest = objectMapper.readValue(json.encode(), PaymentRequest.class);
            paymentService.process(paymentRequest);
        } catch (Exception e) {
            log.error("Error processing payment", e);
            PaymentRequest failedRequest = objectMapper.readValue(json.encode(), PaymentRequest.class);
            enqueue(failedRequest);
        }
    }
}