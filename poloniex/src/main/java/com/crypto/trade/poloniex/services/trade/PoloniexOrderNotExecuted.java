package com.crypto.trade.poloniex.services.trade;

public class PoloniexOrderNotExecuted extends RuntimeException {

    public PoloniexOrderNotExecuted(String message) {
        super(message);
    }

    public PoloniexOrderNotExecuted(String message, Throwable cause) {
        super(message, cause);
    }
}
