package com.pimenta.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private UUID correlationId;
    private BigDecimal amount;
}