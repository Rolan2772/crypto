package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.export.TradesExportService;
import com.crypto.trade.poloniex.services.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Slf4j
public class TradesStorage {

    public static final Duration MAX_AGE = Duration.ofHours(2);

    public static final Comparator<PoloniexTrade> TRADES_COMPARATOR = Comparator.comparing(PoloniexTrade::getTradeTime)
            .thenComparing(PoloniexTrade::getTradeId);

    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private TradesExportService tradesExportService;

    private ConcurrentMap<CurrencyPair, SortedSet<PoloniexTrade>> trades = new ConcurrentHashMap<>();

    public void addTrade(CurrencyPair currency, PoloniexTrade poloniexTrade) {
        initCurrency(currency);
        SortedSet<PoloniexTrade> instrumentTrades = trades.get(currency);
        instrumentTrades.add(poloniexTrade);
        candlesStorage.addTrade(currency, poloniexTrade);
    }

    public SortedSet<PoloniexTrade> getTrades(CurrencyPair currencyPair) {
        return trades.getOrDefault(currencyPair, Collections.emptySortedSet());
    }

    public void initCurrency(CurrencyPair currency) {
        trades.computeIfAbsent(currency, s -> new ConcurrentSkipListSet<>(TRADES_COMPARATOR));
    }

    public void addTradesHistory(CurrencyPair currency, List<PoloniexHistoryTrade> items) {
        Set<PoloniexTrade> currencyTrades = trades.get(currency);
        currencyTrades.addAll(items.stream()
                .map(PoloniexTrade::new)
                .collect(Collectors.toList()));
        candlesStorage.addTradesHistory(currency, currencyTrades);
    }

    public String getLastTrade(CurrencyPair currencyPair) {
        // @TODO: NoSuchElementException
        return trades.getOrDefault(currencyPair, new TreeSet<>()).last().getRate();
    }

    @Scheduled(fixedRate = 1800000)
    public void cleanTrades() {
        CurrencyPair currencyPair = CurrencyPair.BTC_ETH;

        log.info("Cleaning {} stale trades.", currencyPair);
        ZonedDateTime minTime = DateTimeUtils.now().minus(MAX_AGE).truncatedTo(ChronoUnit.MINUTES);
        SortedSet<PoloniexTrade> currencyTrades = getTrades(currencyPair);
        SortedSet<PoloniexTrade> staleTrades = currencyTrades.stream()
                .filter(t -> t.getTradeTime().compareTo(minTime) < 0)
                .collect(Collectors.toCollection(() -> new TreeSet<>(TradesStorage.TRADES_COMPARATOR)));

        int staleElementsCount = staleTrades.size();
        log.info("Found {} trades older than {}.", staleElementsCount, minTime);
        if (staleElementsCount > 0) {
            currencyTrades.removeAll(staleTrades);
            tradesExportService.exportData(TradesExportService.STALE_TRADES_FILE_NAME + currencyPair, staleTrades, true);
        }
    }
}
