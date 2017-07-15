package com.crypto.trade.poloniex.services.integration;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.OrderTrade;
import com.crypto.trade.poloniex.dto.PoloniexOrder;
import com.crypto.trade.poloniex.dto.PoloniexOrderResponse;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.utils.EncodeUtils;
import com.crypto.trade.poloniex.storage.TickersStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TradingService {

    public static final int PRECISION = 8;
    public static final BigDecimal BTC_TRADE_AMOUNT = new BigDecimal("0.000105");

    @Autowired
    private TickersStorage tickersStorage;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private ObjectMapper objectMapper;

    private String key = "ZFNMRR2B-FV6US5OV-BJTQP0LW-ROHIO5FE";
    private String signature = "8662e56994002c90b60e98e1e8e55ec1acbcf049a330ab0b01080f52f6b16c4bd83eda3b7dd50918a266ed3322e0191db4dcd288ecf3eac8d8692d73e28824f5";

    public Optional<PoloniexOrder> placeOrder(TradingRecord tradingRecord, TradingAction action, boolean real) {
        Optional<PoloniexOrder> result = Optional.empty();
        String rate = new BigDecimal(tickersStorage.getLastTrade(CurrencyPair.BTC_ETH))
                .divide(new BigDecimal(real ? 1 : 2), PRECISION, BigDecimal.ROUND_HALF_UP)
                .toString();
        String amount = BTC_TRADE_AMOUNT.divide(new BigDecimal(rate), PRECISION, BigDecimal.ROUND_HALF_UP).toString();
        String body = "nonce=" + Instant.now().toEpochMilli() +
                "&method=GET" +
                "&command=" + (action == TradingAction.ENTERED ? "buy" : "sell") +
                "&currencyPair=" + CurrencyPair.BTC_ETH.name() +
                "&rate=" + rate +
                "&amount=" + amount +
                (action == TradingAction.ENTERED ? "&fillOrKill=1" : "");
        String sign = EncodeUtils.hmacSha512(body, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Key", key);
        headers.set("Sign", sign);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(poloniexProperties.getApiResources().getTradingApi(), requestEntity, String.class);
            log.info("Place order {} response: {}", tradingRecord, response.getBody());
            if (response.getBody().contains("error")) {
                throw new PoloniexResponseException(response.getBody());
            } else {
                PoloniexOrderResponse orderResponse = objectMapper.readValue(response.getBody(), PoloniexOrderResponse.class);
                PoloniexOrder poloniexOrder = new PoloniexOrder(orderResponse.getOrderId(), tradingRecord.getLastOrder());
                if (tradingRecord.getCurrentTrade().isOpened()) {
                    //ZonedDateTime executionTime = awaitOrderExecution(poloniexOrder);
                    //poloniexOrder.setExecutedTime(executionTime);
                }
                result = Optional.of(poloniexOrder);
            }
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            String statusText = e.getStatusText();
            // log or process either of these...
            // you'll probably have to unmarshall the XML manually (only 2 fields so easy)
            log.warn("Failed to place order {} : {}", tradingRecord.getLastOrder(), requestEntity);
            Order order = tradingRecord.getCurrentTrade().getEntry();
            tradingRecord.exit(order.getIndex(), order.getPrice(), order.getAmount());
        } catch (Exception /*RuntimeException*/ e) {
            log.warn("Order has not been executed {} : {}", tradingRecord.getLastOrder(), requestEntity);
            Order order = tradingRecord.getCurrentTrade().getEntry();
            tradingRecord.exit(order.getIndex(), order.getPrice(), order.getAmount());
        }
        return result;
    }

    private ZonedDateTime awaitOrderExecution(PoloniexOrder poloniexOrder) {
        try {
            boolean received = false;
            for (int i = 0; i < 3; i++) {
                TimeUnit.SECONDS.sleep(1);
                List<OrderTrade> orderTrades = getOrderTrades(poloniexOrder);
                log.info("Order '{}' trades: count={}, amount={}, fee={}",
                        orderTrades.size(),
                        orderTrades.stream().map(OrderTrade::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                        orderTrades.stream().map(OrderTrade::getFee).reduce(BigDecimal.ZERO, BigDecimal::add));
                received = orderTrades.isEmpty();
            }
        } catch (InterruptedException e) {
            cancelOrder(poloniexOrder);
            throw new PoloniexOrderNotExecuted("Polonex order execution has been interrupted", e);
        }
        return ZonedDateTime.now(ZoneId.of("GMT+0"));
    }

    public List<OrderTrade> getOrderTrades(PoloniexOrder poloniexOrder) {
        String body = "nonce=" + Instant.now().toEpochMilli() +
                "&method=GET" +
                "&command=returnOrderTrades" +
                "&orderNumber=" + poloniexOrder.getOrderId();
        String sign = EncodeUtils.hmacSha512(body, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Key", key);
        headers.set("Sign", sign);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(poloniexProperties.getApiResources().getTradingApi(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class);
            log.info("Order '{}' trades has been received", poloniexOrder.getOrderId());
            if (response.getBody().contains("error")) {
                throw new IllegalStateException(requestEntity.getBody());
            }
            return Collections.emptyList();//response.getBody(); todo: parse.
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            String statusText = e.getStatusText();
            // log or process either of these...
            // you'll probably have to unmarshall the XML manually (only 2 fields so easy)
            log.warn("Failed to cancel '{}' order: {}", poloniexOrder.getOrderId(), responseBody);
        }
        return Collections.emptyList();
    }

    public boolean cancelOrder(PoloniexOrder poloniexOrder) {
        boolean result = false;
        String body = "nonce=" + Instant.now().toEpochMilli() +
                "&method=GET" +
                "&command=cancelOrder" +
                "&orderNumber=" + poloniexOrder.getOrderId();
        String sign = EncodeUtils.hmacSha512(body, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Key", key);
        headers.set("Sign", sign);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(poloniexProperties.getApiResources().getTradingApi(), requestEntity, String.class);
            log.info("Order '{}' has been cancelled: {}", poloniexOrder.getOrderId(), response.getBody());
            return true;
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            String statusText = e.getStatusText();
            // log or process either of these...
            // you'll probably have to unmarshall the XML manually (only 2 fields so easy)
            log.warn("Failed to cancel '{}' order: {}", poloniexOrder.getOrderId(), responseBody);
        }
        return result;
    }
}
