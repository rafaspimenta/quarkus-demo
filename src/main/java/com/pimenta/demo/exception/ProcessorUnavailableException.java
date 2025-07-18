package com.pimenta.demo.exception;

public class ProcessorUnavailableException extends RuntimeException {
    public ProcessorUnavailableException(String message) {
        super(message);
    }
}