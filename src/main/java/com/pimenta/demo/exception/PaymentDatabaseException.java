package com.pimenta.demo.exception;

import java.sql.SQLException;

public class PaymentDatabaseException extends RuntimeException {

    public PaymentDatabaseException(String message, SQLException e) {
        super(message, e);
    }
}
