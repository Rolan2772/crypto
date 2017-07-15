package com.crypto.trade.poloniex.services.analytics;

public enum TradingAction {

    NO_ACTION, ENTERED, SHOULD_ENTER, EXITED, SHOULD_EXIT;

    public static boolean shouldPlaceOrder(TradingAction action) {
        return action == ENTERED || action == EXITED;
    }
}
