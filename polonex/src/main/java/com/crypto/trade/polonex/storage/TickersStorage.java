package com.crypto.trade.polonex.storage;

import com.crypto.trade.polonex.dto.PolonexTick;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
    private ConcurrentMap<String, List<PolonexTick>> tickers = new ConcurrentHashMap<>();

    public void addTicker(PolonexTick polonexTick) {
        String key = polonexTick.getCurrencyPair();
        lock.lock();
        try {
            tickers.computeIfAbsent(key, s -> new CopyOnWriteArrayList<>());
        } finally {
            lock.unlock();
        }
        tickers.get(polonexTick.getCurrencyPair()).add(polonexTick);
    }

    public TimeSeries generateMinuteCandles(String currencyPair) {
        List<PolonexTick> polonexTicks = new ArrayList<>(tickers.getOrDefault(currencyPair, Collections.emptyList()));
        polonexTicks.sort((o1, o2) -> o1.getTime().compareTo(o2.getTime()));
        Duration duration = Duration.ofMinutes(1);
        List<Tick> ticks = new ArrayList<>();
        for (PolonexTick polonexTick : polonexTicks) {
            if (ticks.isEmpty() || !ticks.get(ticks.size() - 1).inPeriod(polonexTick.getTime())) {
                ticks.add(new Tick(duration, polonexTick.getTime().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)));
            }
            Tick tick = ticks.get(ticks.size() - 1);
            log.trace("Tick {} {}/{}: {}", tick.getBeginTime().toLocalDate(), tick.getBeginTime().toLocalTime(), tick.getEndTime().toLocalTime(), polonexTick.getTime().toLocalTime());
            tick.addTrade(Decimal.valueOf(polonexTick.getQuoteVolume()), Decimal.valueOf(polonexTick.getLast()));
        }
        return new TimeSeries(currencyPair, ticks);
    }
}
