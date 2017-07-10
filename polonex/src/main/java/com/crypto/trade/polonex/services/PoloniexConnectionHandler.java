package com.crypto.trade.polonex.services;

import com.crypto.trade.polonex.dto.PoloniexTrade;
import com.crypto.trade.polonex.storage.TickersStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.time.temporal.ChronoUnit.MINUTES;

@Slf4j
public class PoloniexConnectionHandler implements WebSocketHandler {

    @Autowired
    private ThreadPoolTaskExecutor ticksExecutor;
    @Autowired
    private TickersStorage tickersStorage;

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        log.info("Session started");
        webSocketSession.setTextMessageSizeLimit(1000000);
        webSocketSession.setBinaryMessageSizeLimit(1000000);
        webSocketSession.sendMessage(new TextMessage("{\"command\":\"subscribe\",\"channel\":1002}"));
        webSocketSession.sendMessage(new TextMessage("{\"command\":\"subscribe\",\"channel\":148}"));
        webSocketSession.sendMessage(new TextMessage("{\"command\":\"ticker\",\"channel\":\"BTC_ETH\"}"));
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        String message = webSocketMessage.getPayload().toString();
        if (message.startsWith("[148") && message.contains("[\"t\"")) {
            log.info(message);

            ticksExecutor.submit(() -> {
                try {
                    String[] split = message.split("\"t\"");
                    for (int i = 1; i < split.length; i++) {
                        String[] trade = split[i].split(",");
                        BigDecimal rate = parseRate(trade);
                        LocalDateTime timestamp = parseTimeStamp(trade);


                        Long tradeId = Long.valueOf(trade[1].replace("\"", ""));
                        String type = "1".equals(trade[2]) ? "buy" : "sell";
                        PoloniexTrade pTrade = new PoloniexTrade(tradeId, ZonedDateTime.of(timestamp, ZoneId.of("GMT+0")), trade[4].replace("\"", ""), trade[3].replace("\"", ""), "0", type);
                        tickersStorage.addTrade("BTC_ETH", pTrade);

                        //log.info("rate = {}; timestamp = {}", rate, timestamp);
                    }
                } catch (Exception ex) {
                    log.error("dfgsd", ex);
                }
            });
        }
    }

    private LocalDateTime parseTimeStamp(String[] trade) {
        String tradeTime = trade[5].split("]")[0];
        Date tradeDate = new Date(Long.parseLong(tradeTime) * 1000);
        return LocalDateTime.ofInstant(tradeDate.toInstant(), ZoneId.of("GMT-0")).truncatedTo(MINUTES);
    }

    private BigDecimal parseRate(String[] trade) {
        return new BigDecimal(trade[3].split("\"")[1]).setScale(8, BigDecimal.ROUND_HALF_UP);
    }


    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        log.error("Transport Error", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        log.info("Connection Closed {}" + closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
