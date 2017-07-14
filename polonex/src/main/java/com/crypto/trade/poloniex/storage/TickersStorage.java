package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.dto.PolonexTradeHistoryItem;
import com.crypto.trade.poloniex.dto.PoloniexTick;
import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import eu.verdelhan.ta4j.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class TickersStorage {

    private ReentrantLock addTickLock = new ReentrantLock();
    private ReentrantLock updateCandlesLock = new ReentrantLock();

    @Getter
    private ConcurrentMap<String, List<PoloniexTick>> ticks = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentMap<String, Map<TimeFrame, List<Tick>>> candles = new ConcurrentHashMap<>();
    private Map<TimeFrame, TradingRecord> tradingRecords = Arrays.stream(TimeFrame.values()).collect(Collectors.toMap(timeFrame -> timeFrame, timeFrame -> new TradingRecord()));

    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private StrategiesBuilder strategiesBuilder;

    public void addTick(PoloniexTick poloniexTick) {
        String currency = poloniexTick.getCurrencyPair();
        addCurrencyIfAbsent(currency);
        ticks.get(currency).add(poloniexTick);
        updateCandles(candles.get(currency), poloniexTick);
    }

    private void addCurrencyIfAbsent(String currency) {
        addTickLock.lock();
        try {
            ticks.computeIfAbsent(currency, s -> new CopyOnWriteArrayList<>());
            candles.computeIfAbsent(currency, s -> Arrays.stream(TimeFrame.values())
                    .collect(Collectors.toMap(timeFrame -> timeFrame, timeFrame1 -> new LinkedList<Tick>())));
        } finally {
            addTickLock.unlock();
        }
    }

    public void addTradesHistory(String currency, List<PolonexTradeHistoryItem> items) {
        addCurrencyIfAbsent(currency);
        List<PoloniexTick> currencyTicks = ticks.get(currency);
        currencyTicks.addAll(items.stream().map(i -> new PoloniexTick(i.getTradeId(), i.getDate(), currency, i.getRate(), "", "", "", "", "", false, "", "")).collect(Collectors.toList()));
        currencyTicks.sort((o1, o2) -> {
            int result = o1.getTime().compareTo(o2.getTime());
            if (result == 0) {
                result = o1.getTradeId().compareTo(o2.getTradeId());
            }
            return result;
        });

        // reload candles
        Map<TimeFrame, List<Tick>> currencyCandles = candles.get(currency);
        updateCandlesLock.lock();
        try {
            log.info("Clearing candles with history for {}", currency);
            currencyCandles.values().forEach(List::clear);
            log.info("Updating candles with history for {}", currency);
            currencyTicks.forEach(tick -> updateCandles(currencyCandles, tick));
        } finally {
            updateCandlesLock.unlock();
        }
    }

    private void updateCandles(Map<TimeFrame, List<Tick>> candles, PoloniexTick poloniexTick) {
        candles.entrySet().forEach(e -> {
            updateCandlesLock.lock();
            try {
                //log.debug("Added trade to candles: {}", poloniexTick);
                Tick tick = getLastTick(e.getKey(), e.getValue(), poloniexTick.getTime());
                log.trace("Tick {} {}/{}: {}", tick.getBeginTime().toLocalDate(), tick.getBeginTime().toLocalTime(), tick.getEndTime().toLocalTime(), poloniexTick.getTime().toLocalTime());
                tick.addTrade(Decimal.ONE, Decimal.valueOf(poloniexTick.getLast()));
            } finally {
                updateCandlesLock.unlock();
            }
        });
    }

    private Tick getLastTick(TimeFrame timeFrame, List<Tick> ticks, ZonedDateTime time) {
        if (ticks.isEmpty() || !ticks.get(ticks.size() - 1).inPeriod(time)) {
            if (TimeFrame.ONE_MINUTE == timeFrame && !ticks.isEmpty()) {
                TimeSeries ethOneMinute = new TimeSeries("BTC_ETH", ticks);
                Strategy strategy = strategiesBuilder.buildShortBuyStrategy(ethOneMinute);
                int index = ticks.size() - 1;
                analyticsService.analyzeTick(strategy, ticks.get(index), index, tradingRecords.get(timeFrame));
            }
            ticks.add(new Tick(timeFrame.getFrameDuration(), timeFrame.calculateEndTime(time)));
            log.info("{} candle has been built.", timeFrame);
        }
        return ticks.get(ticks.size() - 1);
    }

    public TimeSeries getCandles(String currency, TimeFrame timeFrame) {
        List<Tick> ticks = candles.getOrDefault(currency, new HashMap<>()).getOrDefault(timeFrame, new LinkedList<>());
        return new TimeSeries(currency, ticks);
    }

    public boolean isNewCandleTick(String currency, TimeFrame timeFrame, ZonedDateTime tickTime) {
        List<Tick> ticks = candles.getOrDefault(currency, new HashMap<>()).getOrDefault(timeFrame, new LinkedList<>());
        return !ticks.isEmpty() && !ticks.get(ticks.size() - 1).inPeriod(tickTime);
    }
}
