package com.pimenta.demo.controller;

import com.pimenta.demo.dto.PaymentRequest;
import com.pimenta.demo.dto.PaymentSummary;
import com.pimenta.demo.queue.PaymentQueueService;
import com.pimenta.demo.service.PaymentService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.time.Instant;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentQueueService paymentQueueService;

    public PaymentController(PaymentService paymentService, PaymentQueueService paymentQueueService) {
        this.paymentService = paymentService;
        this.paymentQueueService = paymentQueueService;
    }

    @POST
    @Path("/payments")
    public Response savePayment(PaymentRequest payment) {
        paymentQueueService.enqueue(payment);
        return Response.accepted().build();
    }

    @POST
    @Path("/purge-payments")
    public Response deleteAllPayments() {
        try {
            paymentService.deleteAllPayments();
            return Response.noContent().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting payments: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/payments-summary")
    public Response getPaymentSummary(
            @QueryParam("fromDate") String fromDateStr,
            @QueryParam("toDate") String toDateStr) {
        try {
            Instant fromDate = fromDateStr != null ? Instant.parse(fromDateStr) : null;
            Instant toDate = toDateStr != null ? Instant.parse(toDateStr) : null;

            PaymentSummary summary = paymentService.getPaymentSummary(fromDate, toDate);
            return Response.ok(summary).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error getting payment summary: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid date format: " + e.getMessage())
                    .build();
        }
    }

}