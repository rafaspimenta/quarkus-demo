package com.pimenta.demo.exception;

public class PaymentQueueException extends RuntimeException {
    public PaymentQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}

