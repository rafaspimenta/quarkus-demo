package com.pimenta.demo.client;

import com.pimenta.demo.dto.ProcessorHealthResponse;
import com.pimenta.demo.dto.ProcessorPaymentRequest;
import com.pimenta.demo.dto.ProcessorPaymentResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "payment-processor")
public interface PaymentProcessorClient {

    @POST
    @Path("/payments")
    ProcessorPaymentResponse processPayment(ProcessorPaymentRequest request);

    @GET
    @Path("/payments/service-health")
    ProcessorHealthResponse health();
}