package com.crypto.trade.poloniex.services.ws;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.storage.TradesStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WsConnectionHandler implements WebSocketHandler {

    @Autowired
    private ThreadPoolTaskExecutor tradesExecutor;
    @Autowired
    private TradesStorage tradesStorage;
    @Autowired
    private ThreadPoolTaskScheduler connectionScheduler;
    private WebSocketSession session;

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        log.info("Session started.");
        webSocketSession.setTextMessageSizeLimit(1000000);
        webSocketSession.setBinaryMessageSizeLimit(1000000);
        webSocketSession.sendMessage(new TextMessage("{\"command\":\"subscribe\",\"channel\":\"" + "BTC_ETH" + "\"}"));
        session = webSocketSession;
        connectionScheduler.scheduleAtFixedRate(() -> {
                    try {
                        keepAlive();
                    } catch (IOException e) {
                        log.error("Sending keep alive message was interrupted.", e);
                    }
                },
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)),
                TimeUnit.MINUTES.toMillis(5));
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        String message = webSocketMessage.getPayload().toString();
        if (message.startsWith("[148") && message.contains("[\"t\"")) {

            //tradesExecutor.submit(() -> {
                log.info(message);
                try {
                    String[] split = message.split("\"t\"");
                    for (int i = 1; i < split.length; i++) {
                        String[] trade = split[i].split(",");
                        BigDecimal rate = parseRate(trade);
                        LocalDateTime timestamp = parseTimeStamp(trade);


                        Long tradeId = Long.valueOf(trade[1].replace("\"", ""));
                        String type = "1".equals(trade[2]) ? "buy" : "sell";
                        PoloniexTrade pTrade = new PoloniexTrade(tradeId, ZonedDateTime.of(timestamp, ZoneOffset.UTC), trade[4].replace("\"", ""), trade[3].replace("\"", ""), "0", type);
                        tradesStorage.addTrade(CurrencyPair.BTC_ETH, pTrade);
                    }
                } catch (Exception ex) {
                    log.error("dfgsd", ex);
                }
            //});
        }
    }

    private LocalDateTime parseTimeStamp(String[] trade) {
        String tradeTime = trade[5].split("]")[0];
        Date tradeDate = new Date(Long.parseLong(tradeTime) * 1000);
        return LocalDateTime.ofInstant(tradeDate.toInstant(), ZoneId.of("GMT-0"));
    }

    private BigDecimal parseRate(String[] trade) {
        return new BigDecimal(trade[3].split("\"")[1]).setScale(8, BigDecimal.ROUND_HALF_UP);
    }

    public void keepAlive() throws IOException {
        if (session != null) {
            session.sendMessage(new TextMessage("."));
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        log.error("Transport Error", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        log.error("Connection Closed {}" + closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
