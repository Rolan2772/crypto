package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import eu.verdelhan.ta4j.Tick;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(of = "timeFrame")
@ToString(of = {"timeFrame", "activeStrategies"})
public class TimeFrameStorage {

    private TimeFrame timeFrame;
    private List<Tick> candles = new LinkedList<>();
    private List<PoloniexStrategy> activeStrategies = new ArrayList<>();

    public List<PoloniexTradingRecord> getAllTradingRecords() {
        return activeStrategies.stream()
                .map(PoloniexStrategy::getTradingRecords)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
