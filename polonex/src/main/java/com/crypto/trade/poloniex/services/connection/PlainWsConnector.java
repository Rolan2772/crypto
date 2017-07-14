package com.crypto.trade.poloniex.services.connection;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PlainWsConnector implements WsConnector {

    @Autowired
    private WsConnectionHandler wsConnectionHandler;
    @Autowired
    private PoloniexProperties poloniexProperties;

    private WebSocketConnectionManager connectionManager;

    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        connectionManager = new WebSocketConnectionManager(client, wsConnectionHandler, poloniexProperties.getApiResources().getWsApi2());
        connectionManager.setAutoStartup(true);
        connectionManager.start();
    }

    public void closeConnection() {
        log.info("Stopping");
        connectionManager.stop();
    }

    @Scheduled(fixedDelay = 5000)
    public void reconnect() throws InterruptedException {
        if (!wsConnectionHandler.isConnected()) {
            log.warn("Reconnecting");
            connectionManager.stop();
            connectionManager.start();
            TimeUnit.SECONDS.sleep(1);
            if (wsConnectionHandler.isConnected()) {
                log.info("Reconnected");
            } else {
                log.error("Can't reconnect");
            }
        }
    }
}
