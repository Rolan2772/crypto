package com.crypto.trade.poloniex.services.connection;

public interface WsConnector {
    void connect();

    void closeConnection();
}
