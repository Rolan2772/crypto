package com.crypto.trade.poloniex.services.trade;

public class PoloniexResponseException extends RuntimeException {

    public PoloniexResponseException(String message) {
        super(message);
    }

    public PoloniexResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
