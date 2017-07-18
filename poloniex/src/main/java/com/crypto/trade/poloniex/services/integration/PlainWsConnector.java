package com.crypto.trade.poloniex.services.integration;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PlainWsConnector implements WsConnector {

    @Autowired
    private WsConnectionHandler wsConnectionHandler;
    @Autowired
    private PoloniexProperties poloniexProperties;

    private WebSocketConnectionManager connectionManager;

    /*public void connect() {

        StandardWebSocketClient client = new StandardWebSocketClient();
        connectionManager = new WebSocketConnectionManager(client, wsConnectionHandler, poloniexProperties.getApi().getWsApi2());
        connectionManager.setAutoStartup(true);

        connectionManager.start();
    }*/

    public void connect() throws IOException, DeploymentException {
        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.PROXY_URI, "http://88.198.230.11:3128");
        client.getProperties().put(ClientProperties.INCOMING_BUFFER_SIZE, 6000000);
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);
        Session session = client.connectToServer(
                PoloniexEndPoint.class,
                URI.create(poloniexProperties.getApi().getWsApi2()));

        if (session.isOpen()) {
            session.getBasicRemote().sendText("{\"command\":\"subscribe\",\"channel\":\"" + CurrencyPair.BTC_ETH + "\"}", true);
        }
    }

    public void closeConnection() {
        log.info("Stopping");
        connectionManager.stop();
    }

    //@Scheduled(fixedDelay = 5000)
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
