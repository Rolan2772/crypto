package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.dto.PoloniexOrder;
import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.*;
import com.crypto.trade.poloniex.services.integration.TradingService;
import eu.verdelhan.ta4j.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class TickersStorage {

    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private StrategiesBuilder strategiesBuilder;
    @Autowired
    private TradingService tradingService;

    private ReentrantLock addTickLock = new ReentrantLock();
    private ReentrantLock updateCandlesLock = new ReentrantLock();

    @Getter
    private ConcurrentMap<CurrencyPair, List<PoloniexTrade>> trades = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentMap<CurrencyPair, Map<TimeFrame, List<Tick>>> candles = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentMap<TimeFrame, TradingRecord> tradingRecords = Arrays.stream(TimeFrame.values())
            .collect(Collectors.toMap(timeFrame -> timeFrame,
                    timeFrame -> new TradingRecord(),
                    (u, v) -> {
                        throw new IllegalStateException(String.format("Duplicate key %s", u));
                    },
                    ConcurrentHashMap::new));
    @Getter
    private ConcurrentMap<TimeFrame, Set<PoloniexOrder>> orders = Arrays.stream(TimeFrame.values())
            .collect(Collectors.toMap(timeFrame -> timeFrame,
                    timeFrame -> new ConcurrentSkipListSet<>((o1, o2) -> o1.getRequestTime().compareTo(o2.getRequestTime())),
                    (u, v) -> {
                        throw new IllegalStateException(String.format("Duplicate key %s", u));
                    },
                    ConcurrentHashMap::new));

    public void addTrade(CurrencyPair currency, PoloniexTrade poloniexTrade) {
        addCurrencyIfAbsent(currency);
        trades.get(currency).add(poloniexTrade);
        updateCandles(candles.get(currency), poloniexTrade);
    }

    private void addCurrencyIfAbsent(CurrencyPair currency) {
        addTickLock.lock();
        try {
            trades.computeIfAbsent(currency, s -> new CopyOnWriteArrayList<>());
            candles.computeIfAbsent(currency, s -> Arrays.stream(TimeFrame.values())
                    .collect(Collectors.toMap(timeFrame -> timeFrame, timeFrame1 -> new LinkedList<Tick>())));
        } finally {
            addTickLock.unlock();
        }
    }

    private void updateCandles(Map<TimeFrame, List<Tick>> candles, PoloniexTrade poloniexTrade) {
        candles.entrySet().forEach(e -> {
            updateCandlesLock.lock();
            try {
                Tick tick = getLastTick(e.getKey(), e.getValue(), poloniexTrade.getTradeTime());
                tick.addTrade(Decimal.valueOf(poloniexTrade.getAmount()), Decimal.valueOf(poloniexTrade.getRate()));
            } finally {
                updateCandlesLock.unlock();
            }
        });
    }

    private Tick getLastTick(TimeFrame timeFrame, List<Tick> ticks, ZonedDateTime time) {
        if (ticks.isEmpty() || !ticks.get(ticks.size() - 1).inPeriod(time)) {
            if (!ticks.isEmpty()) {
                TimeSeries timeSeries = new TimeSeries("BTC_ETH", ticks);
                Strategy strategy = strategiesBuilder.buildShortBuyStrategy(timeSeries);
                int index = ticks.size() - 1;
                TradingRecord tradingRecord = tradingRecords.get(timeFrame);
                TradingAction action = analyticsService.analyzeTick(strategy, ticks.get(index), index, tradingRecord);
                if (TradingAction.shouldPlaceOrder(action)) {
                    Optional<PoloniexOrder> poloniexOrder = tradingService.placeOrder(tradingRecord, action, true);
                    poloniexOrder.ifPresent(order -> orders.get(timeFrame).add(order));
                }
            }
            ticks.add(new Tick(timeFrame.getFrameDuration(), timeFrame.calculateEndTime(time)));
            log.info("{} candle has been built.", timeFrame);
        }
        return ticks.get(ticks.size() - 1);
    }

    public TimeSeries getCandles(CurrencyPair currency, TimeFrame timeFrame) {
        List<Tick> ticks = candles.getOrDefault(currency, new HashMap<>()).getOrDefault(timeFrame, new LinkedList<>());
        return new TimeSeries(currency.name(), ticks);
    }

    public void addTradesHistory(CurrencyPair currency, List<PoloniexHistoryTrade> items) {
        addCurrencyIfAbsent(currency);
        List<PoloniexTrade> currencyTrades = trades.get(currency);
        currencyTrades.addAll(items.stream().map(PoloniexTrade::new).collect(Collectors.toList()));
        currencyTrades.sort((o1, o2) -> o1.getTradeId().compareTo(o2.getTradeId()));

        // reload candles
        Map<TimeFrame, List<Tick>> currencyCandles = candles.get(currency);
        updateCandlesLock.lock();
        try {
            log.info("Clearing candles with history for {}", currency);
            currencyCandles.values().forEach(List::clear);
            log.info("Updating candles with history for {}", currency);
            currencyTrades.forEach(tick -> updateCandles(currencyCandles, tick));
        } finally {
            updateCandlesLock.unlock();
        }
    }

    public String getLastTrade(CurrencyPair currencyPair) {
        List<PoloniexTrade> pairTrades = trades.getOrDefault(currencyPair, Collections.emptyList());
        return pairTrades.get(pairTrades.size() - 1).getRate();
    }
}
