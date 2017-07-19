package com.crypto.trade.poloniex.services.integration.ws;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;

@Slf4j
@Service
public class TyrusWsConnector implements WsConnector {

    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private PoloniexEndPoint poloniexEndPoint;


    @Override
    public void connect() throws IOException, DeploymentException {
        ClientManager client = ClientManager.createClient();
        client.getProperties().put(ClientProperties.PROXY_URI, poloniexProperties.getProxy().getUri());
        client.getProperties().put(ClientProperties.INCOMING_BUFFER_SIZE, 6000000);
        client.getProperties().put(ClientProperties.REDIRECT_ENABLED, true);
        Session session = client.connectToServer(
                poloniexEndPoint,
                URI.create(poloniexProperties.getApi().getWsApi2()));

        if (session.isOpen()) {
            session.getBasicRemote().sendText("{\"command\":\"subscribe\",\"channel\":\"" + CurrencyPair.BTC_ETH + "\"}", true);
        }
    }

    @Override
    public void closeConnection() {
    }
}
