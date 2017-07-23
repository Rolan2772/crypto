package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import eu.verdelhan.ta4j.Tick;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(of = "timeFrame")
@ToString(of = {"timeFrame", "activeStrategies"})
public class TimeFrameStorage {

    private TimeFrame timeFrame;
    private int historyIndex = 0;
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
