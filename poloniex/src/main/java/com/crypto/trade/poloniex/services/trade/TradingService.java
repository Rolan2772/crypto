package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexOrderResponse;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import com.crypto.trade.poloniex.storage.PoloniexOrder;
import com.crypto.trade.poloniex.storage.TradesStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class TradingService {

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

    public Optional<PoloniexOrder> placeOrder(TradingRecord tradingRecord, int index, Order.OrderType direction, BigDecimal volume, boolean real) {
        Optional<PoloniexOrder> poloniexOrder = Optional.empty();
        if (tradingRecord.getCurrentTrade().isNew()) {
            poloniexOrder = buy(tradingRecord, index, direction, volume, real);
        } else if (tradingRecord.getCurrentTrade().isOpened()) {
            poloniexOrder = sell(tradingRecord, index, real);
        } else {
            log.warn("No suitable action found for trading record {} at index {}", tradingRecord, index);
        }
        return poloniexOrder;
    }

    private Optional<PoloniexOrder> buy(TradingRecord tradingRecord, int index, Order.OrderType direction, BigDecimal volume, boolean real) {
        log.info("Processing BUY request {} at index {}", tradingRecord.getCurrentTrade(), index);
        Optional<PoloniexOrder> result = Optional.empty();
        BigDecimal lastTrade = tradesStorage.getLastTrade(CurrencyPair.BTC_ETH);
        BigDecimal rate = real
                ? lastTrade
                : CalculationsUtils.divide(lastTrade, BigDecimal.valueOf(2));
        BigDecimal entryAmount = TradeCalculator.getEntryAmount(volume, rate, direction);

        Map<String, Object> params = new HashMap<>();
        params.put("command", "buy");
        params.put("currencyPair", CurrencyPair.BTC_ETH);
        params.put("rate", rate);
        params.put("amount", entryAmount);
        if (real) {
            params.put("fillOrKill", 1);
        }
        HttpEntity<String> requestEntity = requestHelper.createRequest(params);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(poloniex.getApi().getTradingApi(), requestEntity, String.class);
            log.info("BUY order response: {}", response.getBody());
            if (response.getBody().contains("error")) {
                throw new PoloniexResponseException(response.getBody());
            } else {
                PoloniexOrderResponse orderResponse = objectMapper.readValue(response.getBody(), PoloniexOrderResponse.class);
                log.debug("Order response: {}", orderResponse);
                BigDecimal resultRate = TradeCalculator.getResultRate(orderResponse.getResultingTrades(), rate);
                BigDecimal resultAmount = TradeCalculator.getResultAmount(orderResponse.getResultingTrades(), entryAmount);
                log.debug("Result rate = {}, result amount = {}", resultRate, resultAmount);
                boolean entered = tradingRecord.enter(index, CalculationsUtils.toDecimal(resultRate), CalculationsUtils.toDecimal(resultAmount));
                if (!entered) {
                    log.warn("Trading record entering error at index={}, rate={}, amount={}, fee={}", index, rate, entryAmount, CalculationsUtils.FEE_PERCENT);
                }
                PoloniexOrder poloniexOrder = new PoloniexOrder(orderResponse.getOrderId(), tradingRecord.getLastOrder(), index, TradingAction.ENTERED);
                log.debug("Poloniex order: {}", poloniexOrder);
                result = Optional.of(poloniexOrder);
            }
        } catch (HttpClientErrorException e) {
            log.warn("Failed to place BUY order : {}", e.getResponseBodyAsString());
        } catch (Exception /*RuntimeException*/ e) {
            log.error("BUY order has not been placed.", e);
        }
        return result;
    }

    private Optional<PoloniexOrder> sell(TradingRecord tradingRecord, int index, boolean real) {
        log.info("Processing SELL request {} at index {}", tradingRecord.getCurrentTrade(), index);
        Optional<PoloniexOrder> result = Optional.empty();
        Order entryOrder = tradingRecord.getCurrentTrade().getEntry();
        BigDecimal rate = tradesStorage.getLastTrade(CurrencyPair.BTC_ETH);
        if (!real) {
            rate = rate.multiply(BigDecimal.valueOf(2));
        }

        if (TradeCalculator.canSell(entryOrder, rate)) {
            BigDecimal exitAmount = TradeCalculator.getExitAmount(entryOrder, rate);

            Map<String, Object> params = new HashMap<>();
            params.put("command", "sell");
            params.put("currencyPair", CurrencyPair.BTC_ETH);
            params.put("rate", rate);
            params.put("amount", exitAmount);
            if (real) {
                params.put("fillOrKill", 1);
            }
            HttpEntity<String> requestEntity = requestHelper.createRequest(params);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(poloniex.getApi().getTradingApi(), requestEntity, String.class);
                log.info("SELL order response: {}", response.getBody());
                if (response.getBody().contains("error")) {
                    throw new PoloniexResponseException(response.getBody());
                } else {
                    PoloniexOrderResponse orderResponse = objectMapper.readValue(response.getBody(), PoloniexOrderResponse.class);
                    log.debug("Order response: {}", orderResponse);
                    BigDecimal resultRate = TradeCalculator.getResultRate(orderResponse.getResultingTrades(), rate);
                    BigDecimal resultAmount = TradeCalculator.getResultAmount(orderResponse.getResultingTrades(), exitAmount);
                    log.debug("Result rate = {}, result amount = {}", resultRate, resultAmount);
                    boolean exited = tradingRecord.exit(index, CalculationsUtils.toDecimal(resultRate), CalculationsUtils.toDecimal(resultAmount));
                    if (!exited) {
                        log.warn("Trading record exiting error at index {}", index);
                    }
                    PoloniexOrder poloniexOrder = new PoloniexOrder(orderResponse.getOrderId(), tradingRecord.getLastOrder(), index, TradingAction.EXITED);
                    log.debug("Poloniex order: {}", poloniexOrder);
                    result = Optional.of(poloniexOrder);
                }
            } catch (HttpClientErrorException e) {
                log.warn("Failed to place BUY order : {}", e.getResponseBodyAsString());
            } catch (Exception /*RuntimeException*/ e) {
                log.error("SELL order has not been placed.", e);
            }
        } else {
            log.warn("SELL profit didn't reach minimum value: {}", CalculationsUtils.MIN_PROFIT_PERCENT);
        }
        return result;
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
