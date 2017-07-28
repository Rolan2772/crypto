package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.verdelhan.ta4j.Strategy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

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
    private List<PoloniexTradingRecord> tradingRecords = new ArrayList<>();

    public PoloniexStrategy(String name, Strategy strategy, TimeFrame timeFrame, List<PoloniexTradingRecord> tradingRecords) {
        this.name = name;
        this.strategy = strategy;
        this.timeFrame = timeFrame;
        this.tradingRecords.addAll(tradingRecords);
    }

    public void addTradingRecord(PoloniexTradingRecord tradingRecord) {
        this.getTradingRecords().add(tradingRecord);
    }
}
