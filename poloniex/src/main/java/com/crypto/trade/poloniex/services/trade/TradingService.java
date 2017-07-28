package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.OrderTrade;
import com.crypto.trade.poloniex.dto.PoloniexOrder;
import com.crypto.trade.poloniex.dto.PoloniexOrderResponse;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.storage.TradesStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class TradingService {

    public static final int PRECISION = 8;
    // @TODO: Both BUY/SELL can have 0.25% fee
    public static final BigDecimal FEE_PERCENT = new BigDecimal("0.0025");
    public static final BigDecimal AFTER_FEE_PERCENT = BigDecimal.ONE.subtract(FEE_PERCENT);
    public static final BigDecimal MIN_PROFIT_PERCENT = new BigDecimal("1.01");

    @Autowired
    private TradesStorage tradesStorage;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PoloniexProperties poloniex;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PoloniexRequestHelper requestHelper;

    public Optional<PoloniexOrder> placeOrder(TradingRecord tradingRecord, int index, TradingAction action, BigDecimal volume, boolean real) {
        Optional<PoloniexOrder> poloniexOrder = Optional.empty();
        if (tradingRecord.getCurrentTrade().isNew()) {
            poloniexOrder = buy(tradingRecord, index, volume, real);
        } else if (tradingRecord.getCurrentTrade().isOpened()) {
            poloniexOrder = sell(tradingRecord, index, real);
        } else {
            log.warn("No suitable action found for trading record {} at index {}", tradingRecord, index);
        }
        return poloniexOrder;
    }

    private Optional<PoloniexOrder> buy(TradingRecord tradingRecord, int index, BigDecimal volume, boolean real) {
        log.info("Processing BUY request {} at index {}", tradingRecord.getCurrentTrade(), index);
        Optional<PoloniexOrder> result = Optional.empty();
        String lastTrade = tradesStorage.getLastTrade(CurrencyPair.BTC_ETH);
        String rate = real
                ? lastTrade
                : new BigDecimal(lastTrade).divide(new BigDecimal(2), PRECISION, BigDecimal.ROUND_HALF_UP).toString();
        String amount = volume.divide(new BigDecimal(rate), PRECISION, BigDecimal.ROUND_HALF_UP).toString();

        Map<String, Object> params = new HashMap<>();
        params.put("command", "buy");
        params.put("currencyPair", CurrencyPair.BTC_ETH);
        params.put("rate", rate);
        params.put("amount", amount);
        if (real) {
            params.put("fillOrKill", 1);
        }
        HttpEntity<String> requestEntity = requestHelper.createRequest(params);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(poloniex.getApi().getTradingApi(), requestEntity, String.class);
            log.info("BUY order {} response: {}", tradingRecord, response.getBody());
            if (response.getBody().contains("error")) {
                throw new PoloniexResponseException(response.getBody());
            } else {
                // @TODO: read trades price
                // BUY order eu.verdelhan.ta4j.TradingRecord@1ccd0c85 response: {"orderNumber":"320339376112","resultingTrades":[{"amount":"0.00132743","date":"2017-07-25 07:55:16","rate":"0.07909999","total":"0.00010499","tradeID":"31227533","type":"buy"}]}
                PoloniexOrderResponse orderResponse = objectMapper.readValue(response.getBody(), PoloniexOrderResponse.class);
                // @TODO: non transparent buy amount fee calculations
                BigDecimal amontAfterFee = new BigDecimal(amount).multiply(AFTER_FEE_PERCENT).setScale(PRECISION, BigDecimal.ROUND_HALF_UP);
                boolean entered = tradingRecord.enter(index, Decimal.valueOf(rate), Decimal.valueOf(amontAfterFee.toString()));
                if (!entered) {
                    log.warn("Trading record entering error at index={}, rate={}, amount={}/{}", index, rate, amount, amontAfterFee);
                }
                PoloniexOrder poloniexOrder = new PoloniexOrder(orderResponse.getOrderId(), tradingRecord.getLastOrder(), index, TradingAction.ENTERED);
                log.debug("Poloniex order: {}", poloniexOrder);
                result = Optional.of(poloniexOrder);
            }
        } catch (HttpClientErrorException e) {
            log.warn("Failed to place BUY order {} : {}", params, e.getResponseBodyAsString());
        } catch (Exception /*RuntimeException*/ e) {
            log.warn("BUY order has not been placed {} : {}", params, requestEntity);
        }
        return result;
    }

    private Optional<PoloniexOrder> sell(TradingRecord tradingRecord, int index, boolean real) {
        log.info("Processing SELL request {} at index {}", tradingRecord.getCurrentTrade(), index);
        Optional<PoloniexOrder> result = Optional.empty();
        Order entryOrder = tradingRecord.getCurrentTrade().getEntry();
        BigDecimal lastTrade = new BigDecimal(tradesStorage.getLastTrade(CurrencyPair.BTC_ETH));
        if (!real) {
            lastTrade = lastTrade.multiply(new BigDecimal(2));
        }

        // Profit calculations
        BigDecimal openPrice = new BigDecimal(entryOrder.getPrice().toString());
        // @TODO: non transparent buy amount fee calculations
        BigDecimal buyAmount = new BigDecimal(entryOrder.getAmount().toString()).divide(AFTER_FEE_PERCENT, PRECISION, BigDecimal.ROUND_HALF_UP);
        BigDecimal buySpent = openPrice.multiply(buyAmount);

        // @TODO: SELL gain result in 1 + 0.25 + 0.25 percent
        BigDecimal sellGain = lastTrade.multiply(new BigDecimal(entryOrder.getAmount().toString())).multiply(AFTER_FEE_PERCENT);
        log.debug("Open price = {}, last price = {}, buy amount = {}, but spent = {}, sell gain = {}", openPrice, lastTrade, buyAmount, buySpent, sellGain);
        BigDecimal diff = sellGain.divide(buySpent, PRECISION, BigDecimal.ROUND_HALF_UP);
        log.info("Expected SELL profit: {}", diff);
        if (diff.compareTo(MIN_PROFIT_PERCENT) > 0) {
            String rate = lastTrade.toString();
            String amount = entryOrder.getAmount().toString();

            Map<String, Object> params = new HashMap<>();
            params.put("command", "sell");
            params.put("currencyPair", CurrencyPair.BTC_ETH);
            params.put("rate", rate);
            params.put("amount", amount);
            if (real) {
                params.put("fillOrKill", 1);
            }
            HttpEntity<String> requestEntity = requestHelper.createRequest(params);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(poloniex.getApi().getTradingApi(), requestEntity, String.class);
                log.info("SELL order {} response: {}", tradingRecord, response.getBody());
                if (response.getBody().contains("error")) {
                    throw new PoloniexResponseException(response.getBody());
                } else {
                    // @TODO: read trades price
                    // SELL order eu.verdelhan.ta4j.TradingRecord@1ccd0c85 response: {"orderNumber":"320358630838","resultingTrades":[{"amount":"0.00132411","date":"2017-07-25 08:50:09","rate":"0.08086999","total":"0.00010708","tradeID":"31232248","type":"sell"}]}
                    PoloniexOrderResponse orderResponse = objectMapper.readValue(response.getBody(), PoloniexOrderResponse.class);
                    // @TODO: non transparent fee calculations
                    BigDecimal rateAfterFee = new BigDecimal(rate).multiply(AFTER_FEE_PERCENT).setScale(PRECISION, BigDecimal.ROUND_HALF_UP);
                    boolean exited = tradingRecord.exit(index, Decimal.valueOf(rateAfterFee.toString()), Decimal.valueOf(amount));
                    if (!exited) {
                        log.warn("Trading record exiting error at index={}, rate={}/{}, amount={}", index, rate, rateAfterFee, amount);
                    }
                    PoloniexOrder poloniexOrder = new PoloniexOrder(orderResponse.getOrderId(), tradingRecord.getLastOrder(), index, TradingAction.EXITED);
                    log.debug("Poloniex order: {}", poloniexOrder);
                    result = Optional.of(poloniexOrder);
                }
            } catch (HttpClientErrorException e) {
                String responseBody = e.getResponseBodyAsString();
                log.warn("Failed to place SELL order {} : {}", params, requestEntity);
            } catch (Exception /*RuntimeException*/ e) {
                log.warn("SELL order has not been placed {} : {}", params, requestEntity);
            }
        } else {
            log.warn("SELL profit didn't reach minimum value: {}/{}", diff, MIN_PROFIT_PERCENT);
        }
        return result;
    }

    public List<OrderTrade> getOrderTrades(PoloniexOrder poloniexOrder) {
        Map<String, Object> params = new HashMap<>();
        params.put("command", "returnOrderTrades");
        params.put("orderNumber", poloniexOrder.getOrderId());
        HttpEntity<String> requestEntity = requestHelper.createRequest(params);

        try {
            ResponseEntity<String> response = restTemplate.exchange(poloniex.getApi().getTradingApi(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class);
            log.info("Order '{}' trades has been received", poloniexOrder.getOrderId());
            if (response.getBody().contains("error")) {
                throw new PoloniexResponseException(requestEntity.getBody());
            }
            return Collections.emptyList();//response.getBody(); todo: parse.
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.warn("Failed to cancel '{}' order: {}", poloniexOrder.getOrderId(), responseBody);
        }
        return Collections.emptyList();
    }

    public String cancelOrder(PoloniexOrder poloniexOrder) {
        String result = "empty";

        Map<String, Object> params = new HashMap<>();
        params.put("command", "cancelOrder");
        params.put("orderNumber", poloniexOrder.getOrderId());
        HttpEntity<String> requestEntity = requestHelper.createRequest(params);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(poloniex.getApi().getTradingApi(), requestEntity, String.class);
            result = response.getBody();
            if (response.getBody().contains("error")) {
                throw new PoloniexResponseException(response.getBody());
            }
            log.info("Order '{}' has been cancelled: {}", poloniexOrder.getOrderId(), response.getBody());
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            result = responseBody;
            log.warn("Failed to cancel '{}' order: {}", poloniexOrder.getOrderId(), responseBody);
        } catch (PoloniexResponseException e) {
            log.warn("Failed to cancel '{}' order: {}", poloniexOrder.getOrderId(), e.getMessage());

        }
        return result;
    }

}
