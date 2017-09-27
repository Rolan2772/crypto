package com.crypto.trade.poloniex.storage.model;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import eu.verdelhan.ta4j.Tick;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(of = "timeFrame")
@ToString(of = {"timeFrame", "activeStrategies"})
public class TimeFrameStorage {

    private ReentrantLock updateLock = new ReentrantLock();
    private int historyIndex;
    private TimeFrame timeFrame;
    private List<Tick> candles = new LinkedList<>();
    private List<PoloniexStrategy> activeStrategies = new ArrayList<>();

    public TimeFrameStorage(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public List<PoloniexTradingRecord> getAllTradingRecords() {
        return activeStrategies.stream()
                .map(PoloniexStrategy::getTradingRecords)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void addStrategy(PoloniexStrategy poloniexStrategy) {
        this.activeStrategies.add(poloniexStrategy);
    }
}
