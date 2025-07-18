package com.pimenta.demo.dto;

import com.pimenta.demo.model.ProcessorType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ProcessorPaymentResponse {
    private ProcessorType processorType;
    private String message;
}