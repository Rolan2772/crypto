package com.crypto.trade.poloniex.services.integration;

public interface WsConnector {

    void connect();

    void closeConnection();
}
