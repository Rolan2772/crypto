package com.crypto.trade.poloniex.services.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncUtils {

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Sleeping was interrupted.", e);
        }
    }
}