package com.pimenta.demo.client;

import com.pimenta.demo.dto.ProcessorHealthResponse;
import com.pimenta.demo.dto.ProcessorPaymentRequest;
import com.pimenta.demo.dto.ProcessorPaymentResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "payment-processor-fallback")
public interface PaymentProcessorFallbackClient {

    @POST
    @Path("/payments")
    ProcessorPaymentResponse processPayment(ProcessorPaymentRequest request);

    @GET
    @Path("/payments/service-health")
    ProcessorHealthResponse health();
}