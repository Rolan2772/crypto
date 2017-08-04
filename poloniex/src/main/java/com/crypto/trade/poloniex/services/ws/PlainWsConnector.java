package com.crypto.trade.poloniex.services.ws;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PlainWsConnector implements WsConnector {

    @Autowired
    private WsConnectionHandler wsConnectionHandler;
    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ThreadPoolTaskScheduler connectionScheduler;

    private WebSocketConnectionManager connectionManager;

    // @TODO: no connection for some time 2017-08-04 05:12 - 2017-08-04 8:29, then everything is ok
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        connectionManager = new WebSocketConnectionManager(client, wsConnectionHandler, poloniexProperties.getApi().getWsApi2());
        connectionManager.setAutoStartup(true);

        connectionManager.start();
        connectionScheduler.scheduleWithFixedDelay(() -> {
                    try {
                        reconnect();
                    } catch (InterruptedException e) {
                        log.error("Reconnection attempt was interrupted.", e);
                    }
                }, new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)),
                TimeUnit.MINUTES.toMillis(1));
    }

    public void closeConnection() {
        log.info("Stopping");
        connectionManager.stop();
    }

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
