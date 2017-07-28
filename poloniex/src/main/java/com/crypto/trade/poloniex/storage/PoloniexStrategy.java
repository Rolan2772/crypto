package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.verdelhan.ta4j.Strategy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Value
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
@ToString(of = {"name"})
public class PoloniexStrategy {

    private String name;
    @JsonIgnore
    private Strategy strategy;
    private TimeFrame timeFrame;
    private BigDecimal tradeVolume;
    private List<PoloniexTradingRecord> tradingRecords = new ArrayList<>();

    public PoloniexStrategy(PoloniexStrategy poloniexStrategy, List<PoloniexTradingRecord> tradingRecords) {
        this.name = poloniexStrategy.getName();
        this.strategy = poloniexStrategy.getStrategy();
        this.timeFrame = poloniexStrategy.getTimeFrame();
        this.tradeVolume = poloniexStrategy.getTradeVolume();
        this.tradingRecords.addAll(tradingRecords);
    }

    public void addTradingRecord(PoloniexTradingRecord tradingRecord) {
        this.getTradingRecords().add(tradingRecord);
    }
}
