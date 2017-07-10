package com.crypto.trade.polonex.services;

import com.crypto.trade.polonex.config.properties.PoloniexProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Service
public class PoloniexConnectionService {

    @Autowired
    private PoloniexConnectionHandler poloniexConnectionHandler;
    @Autowired
    private PoloniexProperties poloniexProperties;

    private WebSocketConnectionManager connectionManager;

    @PostConstruct
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        connectionManager = new WebSocketConnectionManager(client, poloniexConnectionHandler, poloniexProperties.getApiResources().getWsApi2());
        connectionManager.setAutoStartup(true);
        connectionManager.start();
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping");
        connectionManager.stop();
    }
}
