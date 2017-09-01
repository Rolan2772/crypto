package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.AnalyticsCache;
import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.trade.TradingService;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class CandlesStorage {

    @Autowired
    private AnalyticsService realTimeAnalyticsService;
    @Autowired
    private ThreadPoolTaskExecutor strategyExecutor;
    @Autowired
    private TradingService tradingService;
    @Autowired
    private PoloniexProperties poloniexProperties;
    @Autowired
    private AnalyticsCache analyticsCache;

    private ConcurrentMap<CurrencyPair, List<TimeFrameStorage>> candles = new ConcurrentHashMap<>();

    public void initCurrency(CurrencyPair currencyPair, List<TimeFrameStorage> timeFrameData) {
        candles.put(currencyPair, timeFrameData);
    }

    public void addTrade(CurrencyPair currency, PoloniexTrade poloniexTrade) {
        candles.computeIfPresent(currency, (currencyPair, candles) -> {
            candles.forEach(timeFrameStorage -> updateCandles(currencyPair, timeFrameStorage, poloniexTrade, false));
            return candles;
        });
    }

    private void updateCandles(CurrencyPair currencyPair, TimeFrameStorage timeFrameStorage, PoloniexTrade poloniexTrade, boolean isHistoryTick) {
        timeFrameStorage.getUpdateLock().lock();
        try {
            Tick candle = findCandle(timeFrameStorage.getCandles(), poloniexTrade.getTradeTime())
                    .orElseGet(new NewCandleSupplier(timeFrameStorage,
                            poloniexTrade.getTradeTime(),
                            strategyExecutor,
                            realTimeAnalyticsService,
                            tradingService,
                            poloniexProperties.getTradeConfig().isRealPrice(),
                            isHistoryTick,
                            analyticsCache,
                            currencyPair));
            candle.addTrade(Decimal.valueOf(poloniexTrade.getAmount()), Decimal.valueOf(poloniexTrade.getRate()));
        } finally {
            timeFrameStorage.getUpdateLock().unlock();
        }
    }

    private Optional<Tick> findCandle(List<Tick> candles, ZonedDateTime tradeTime) {
        Optional<Tick> candle = Optional.empty();
        int size = candles.size();
        if (!candles.isEmpty()) {
            for (int index = Math.max(0, candles.size() - 5); index < size && !candle.isPresent(); index++) {
                Tick currentCandle = candles.get(index);
                if (currentCandle.inPeriod(tradeTime)) {
                    candle = Optional.of(currentCandle);
                }
            }
        }
        return candle;
    }

    public List<TimeFrameStorage> getData(CurrencyPair currencyPair) {
        return candles.getOrDefault(currencyPair, Collections.emptyList());
    }

    public void addTradesHistory(CurrencyPair currency, Set<PoloniexTrade> poloniexTrades) {
        candles.computeIfPresent(currency, (currencyPair, candles) -> {
            candles.forEach(timeFrameStorage -> {
                timeFrameStorage.getUpdateLock().lock();
                try {
                    TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
                    // @TODO: add history without candles recreation
                    log.info("Clearing {} candles with history for {}", timeFrame, currency);
                    timeFrameStorage.getCandles().clear();
                    log.info("Updating candles with history for {}", currency);
                    poloniexTrades.forEach(poloniexTrade -> updateCandles(currencyPair, timeFrameStorage, poloniexTrade, true));
                } finally {
                    timeFrameStorage.getUpdateLock().unlock();
                }
            });
            return candles;
        });
    }

    public List<PoloniexStrategy> getActiveStrategies(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return candles.getOrDefault(currencyPair, Collections.emptyList())
                .stream()
                .filter(timeFrameStorage -> timeFrameStorage.getTimeFrame() == timeFrame)
                .findFirst()
                .orElse(new TimeFrameStorage(TimeFrame.ONE_MINUTE))
                .getActiveStrategies();
    }

    public Set<CurrencyPair> getCurrencies() {
        return candles.keySet();
    }
}