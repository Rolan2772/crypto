package com.crypto.trade.polonex.storage;

import com.crypto.trade.polonex.dto.PoloniexTick;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalUnit;
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

    public TimeSeries generateCandles(String currency, Long duration, TemporalUnit unit, ChronoField field) {
        List<PoloniexTick> poloniexTicks = new ArrayList<>(ticks.getOrDefault(currency, Collections.emptyList()));
        poloniexTicks.sort(Comparator.comparing(PoloniexTick::getTime));
        List<Tick> ticks = createCandles(poloniexTicks, duration, unit, field);
        return new TimeSeries(currency, ticks);
    }

    private List<Tick> createCandles(List<PoloniexTick> poloniexTicks, Long duration, TemporalUnit unit, ChronoField field) {
        List<Tick> ticks = new ArrayList<>();
        Duration tickDuration = Duration.of(duration, unit);
        for (PoloniexTick poloniexTick : poloniexTicks) {
            if (ticks.isEmpty() || !ticks.get(ticks.size() - 1).inPeriod(poloniexTick.getTime())) {
                ZonedDateTime candleEndTime = calculateEndTime(poloniexTick, duration, unit, field);
                ticks.add(new Tick(tickDuration, candleEndTime));
            }
            Tick tick = ticks.get(ticks.size() - 1);
            log.trace("Tick {} {}/{}: {}", tick.getBeginTime().toLocalDate(), tick.getBeginTime().toLocalTime(), tick.getEndTime().toLocalTime(), poloniexTick.getTime().toLocalTime());
            tick.addTrade(Decimal.ONE, Decimal.valueOf(poloniexTick.getLast()));
        }
        return ticks;
    }

    private ZonedDateTime calculateEndTime(PoloniexTick tick, Long duration, TemporalUnit unit, ChronoField chronoField) {
        ZonedDateTime truncatedTime = tick.getTime().truncatedTo(unit);
        Long diff = duration - truncatedTime.get(chronoField) % duration;
        return truncatedTime.plus(diff, unit);
    }
}
