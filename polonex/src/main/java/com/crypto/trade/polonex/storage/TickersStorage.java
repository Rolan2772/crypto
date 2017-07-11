package com.crypto.trade.polonex.storage;

import com.crypto.trade.polonex.dto.PoloniexTick;
import com.crypto.trade.polonex.services.analytics.TimeFrame;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TickersStorage {

    private ReentrantLock lock = new ReentrantLock();

    @Getter
    // @TODO: choose correct collection for Ticks storage
    private ConcurrentMap<String, List<PoloniexTick>> ticks = new ConcurrentHashMap<>();

    public void addTicker(PoloniexTick poloniexTick) {
        String key = poloniexTick.getCurrencyPair();
        lock.lock();
        try {
            ticks.computeIfAbsent(key, s -> new CopyOnWriteArrayList<>());
        } finally {
            lock.unlock();
        }
        ticks.get(poloniexTick.getCurrencyPair()).add(poloniexTick);
    }

    public TimeSeries generateCandles(String currency, TimeFrame timeFrame) {
        List<PoloniexTick> poloniexTicks = new ArrayList<>(ticks.getOrDefault(currency, Collections.emptyList()));
        poloniexTicks.sort(Comparator.comparing(PoloniexTick::getTime));
        List<Tick> ticks = createCandles(poloniexTicks, timeFrame);
        return new TimeSeries(currency, ticks);
    }

    private List<Tick> createCandles(List<PoloniexTick> poloniexTicks, TimeFrame timeFrame) {
        List<Tick> ticks = new ArrayList<>();
        for (PoloniexTick poloniexTick : poloniexTicks) {
            if (ticks.isEmpty() || !ticks.get(ticks.size() - 1).inPeriod(poloniexTick.getTime())) {
                ticks.add(new Tick(timeFrame.getFrameDuration(), timeFrame.calculateEndTime(poloniexTick.getTime())));
            }
            Tick tick = ticks.get(ticks.size() - 1);
            log.trace("Tick {} {}/{}: {}", tick.getBeginTime().toLocalDate(), tick.getBeginTime().toLocalTime(), tick.getEndTime().toLocalTime(), poloniexTick.getTime().toLocalTime());
            tick.addTrade(Decimal.ONE, Decimal.valueOf(poloniexTick.getLast()));
        }
        return ticks;
    }


}
