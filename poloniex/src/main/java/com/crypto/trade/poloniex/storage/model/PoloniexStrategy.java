package com.crypto.trade.poloniex.storage.model;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.model.StrategyConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value
@EqualsAndHashCode(of = "name")
@ToString(of = {"name"})
public class PoloniexStrategy {

    private String name;
    @JsonIgnore
    private Strategy strategy;
    private TimeFrame timeFrame;
    private Order.OrderType direction;
    private BigDecimal tradeVolume;
    private List<PoloniexTradingRecord> tradingRecords;

    public PoloniexStrategy(PoloniexStrategy poloniexStrategy, List<PoloniexTradingRecord> tradingRecords) {
        this.name = poloniexStrategy.getName();
        this.strategy = poloniexStrategy.getStrategy();
        this.timeFrame = poloniexStrategy.getTimeFrame();
        this.direction = poloniexStrategy.getDirection();
        this.tradeVolume = poloniexStrategy.getTradeVolume();
        this.tradingRecords = new ArrayList<>(tradingRecords);
    }

    public PoloniexStrategy(String name, StrategyConfig config, Strategy strategy) {
        this.name = name;
        this.timeFrame = config.getTimeFrame();
        this.direction = config.getDirection();
        this.tradeVolume = config.getVolume();
        this.strategy = strategy;
        this.tradingRecords = IntStream.rangeClosed(1, config.getRecordsCount())
                .mapToObj(index -> new PoloniexTradingRecord(index, name, config.getDirection()))
                .collect(Collectors.toList());
    }
}
