package com.pimenta.demo.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class Payment {
    private UUID id;
    private UUID correlationId;
    private BigDecimal amount;
    private Instant requestedAt;
    private ProcessorType processorType;
    private Instant createdAt;
}