package com.pimenta.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@RegisterForReflection
public class PaymentSummary {
    @JsonProperty("default")
    private ProcessorSummary defaultProcessor;
    private ProcessorSummary fallback;

    @Data
    @Builder
    public static class ProcessorSummary {
        private long totalRequests;
        private BigDecimal totalAmount;
    }
}