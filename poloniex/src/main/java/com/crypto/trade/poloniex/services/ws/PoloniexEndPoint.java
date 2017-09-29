package com.crypto.trade.poloniex.services.ws;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.storage.TradesStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@ClientEndpoint
public class PoloniexEndPoint {

    @Autowired
    private TradesStorage tradesStorage;

    @OnOpen
    public void onOpen(Session session) {
        try {
            String msg = "{\"command\":\"subscribe\",\"channel\":\"" + CurrencyPair.BTC_ETH + "\"}";
            log.info("Sending message to endpoint: {}", msg);
            session.getBasicRemote().sendText(msg, true);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (message.startsWith("[148") && message.contains("[\"t\"")) {

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
                log.error("Failed to process message: " + message, ex);
            }
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

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("Disconnected: " + closeReason);
    }

}

