package com.crypto.trade.poloniex.services.ws;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TyrusWsConnector implements WsConnector {

    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private PoloniexEndPoint poloniexEndPoint;
    @Autowired
    private ThreadPoolTaskScheduler connectionScheduler;

    private Session session;

    @Override
    public void connect() throws IOException, DeploymentException {
        ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

            private AtomicInteger reconnectCount = new AtomicInteger(0);

            @Override
            public boolean onDisconnect(CloseReason closeReason) {
                boolean shouldReconnect = reconnectCount.incrementAndGet() < 20;
                log.info("### Reconnecting after disconnect... (reconnect count: {}): {}", reconnectCount.get(), shouldReconnect);
                return shouldReconnect;
            }

            @Override
            public long getDelay() {
                return 5;
            }
        };

        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.PROXY_URI, poloniexProperties.getProxy().getUri());
        client.getProperties().put(ClientProperties.INCOMING_BUFFER_SIZE, 6000000);
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);
        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
        client.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

        session = client.connectToServer(
                poloniexEndPoint,
                URI.create(poloniexProperties.getApi().getWsApi2()));

        connectionScheduler.scheduleWithFixedDelay(() -> {
                    try {
                        keepAlive();
                    } catch (IOException e) {
                        log.error("Reconnection attempt was interrupted.", e);
                    }
                },
                TimeUnit.MINUTES.toMillis(5));
    }

    public void keepAlive() throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(".", true);
        }
    }

    @Override
    public void closeConnection() {
        log.debug("Closing connection");
    }
}
