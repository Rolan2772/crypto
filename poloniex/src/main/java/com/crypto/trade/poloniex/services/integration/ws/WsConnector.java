package com.crypto.trade.poloniex.services.integration.ws;

import javax.websocket.DeploymentException;
import java.io.IOException;

public interface WsConnector {

    void connect() throws IOException, DeploymentException;

    void closeConnection();
}
