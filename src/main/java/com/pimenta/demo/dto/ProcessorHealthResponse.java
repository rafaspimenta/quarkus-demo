package com.pimenta.demo.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ProcessorHealthResponse {
    private boolean failing;
    private int minResponseTime;
}