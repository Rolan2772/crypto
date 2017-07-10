package com.crypto.trade.polonex.storage;

import com.crypto.trade.polonex.dto.PoloniexTrade;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TickersStorage {

    private ReentrantLock lock = new ReentrantLock();

    @Getter
    // @TODO: choose correct collection for Ticks storage
    private ConcurrentMap<String, List<PoloniexTrade>> trades = new ConcurrentHashMap<>();

    public void addTrade(String currency, PoloniexTrade trade) {
        lock.lock();
        try {
            trades.computeIfAbsent(currency, s -> new CopyOnWriteArrayList<PoloniexTrade>());
        } finally {
            lock.unlock();
        }
        trades.get(currency).add(trade);
    }

    public TimeSeries generateCandles(String currency, Long duration, TemporalUnit unit, ChronoField field) {
        List<PoloniexTrade> poloniexTrades = new LinkedList<>(trades.getOrDefault(currency, Collections.emptyList()));
        poloniexTrades.sort(Comparator.comparing(PoloniexTrade::getTradeTime));
        List<Tick> ticks = createCandles(poloniexTrades, duration, unit, field);
        return new TimeSeries(currency, ticks);
    }

    private List<Tick> createCandles(List<PoloniexTrade> poloniexTrades, Long duration, TemporalUnit unit, ChronoField field) {
        List<Tick> ticks = new ArrayList<>();
        Duration tickDuration = Duration.of(duration, unit);
        for (PoloniexTrade poloniexTrade : poloniexTrades) {
            if (ticks.isEmpty() || !ticks.get(ticks.size() - 1).inPeriod(poloniexTrade.getTradeTime())) {
                ZonedDateTime candleEndTime = calculateEndTime(poloniexTrade, duration, unit, field);
                ticks.add(new Tick(tickDuration, candleEndTime));
            }
            Tick tick = ticks.get(ticks.size() - 1);
            log.trace("Tick {} {}/{}: {}", tick.getBeginTime().toLocalDate(),
                    tick.getBeginTime().toLocalTime(),
                    tick.getEndTime().toLocalTime(),
                    poloniexTrade.getTradeTime());
            tick.addTrade(Decimal.valueOf(poloniexTrade.getTotal()), Decimal.valueOf(poloniexTrade.getRate()));
        }
        return ticks;
    }

    private ZonedDateTime calculateEndTime(PoloniexTrade trade, Long duration, TemporalUnit unit, ChronoField chronoField) {
        ZonedDateTime truncatedTime = trade.getTradeTime().truncatedTo(unit);
        Long diff = duration - truncatedTime.get(chronoField) % duration;
        return truncatedTime.plus(diff, unit);
    }
}
