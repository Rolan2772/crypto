package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.dto.PoloniexOrder;
import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.integration.TradingService;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TradingRecord;
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
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CandlesStorage {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private AnalyticsService realTimeAnalyticsService;
    @Autowired
    private TradingService tradingService;
    @Autowired
    private ThreadPoolTaskExecutor tradesExecutor;

    private ReentrantLock updateCandlesLock = new ReentrantLock();
    private ConcurrentMap<CurrencyPair, List<TimeFrameStorage>> candles = new ConcurrentHashMap<>();

    public List<PoloniexStrategy> getActiveStrategies(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return candles.getOrDefault(currencyPair, Collections.emptyList())
                .stream()
                .filter(timeFrameStorage -> timeFrameStorage.getTimeFrame() == timeFrame)
                .findFirst()
                .orElse(new TimeFrameStorage(TimeFrame.ONE_MINUTE))
                .getActiveStrategies();
    }

    public void addTrade(CurrencyPair currency, PoloniexTrade poloniexTrade) {
        candles.computeIfPresent(currency, (currencyPair, candles) -> {
            // @TODO: implement update in new thread
            candles.forEach(timeFrameStorage -> updateCandles(timeFrameStorage, poloniexTrade, false));
            return candles;
        });
    }

    public void setupCurrency(CurrencyPair currencyPair, List<TimeFrameStorage> timeFrameData) {
        candles.put(currencyPair, timeFrameData);
    }

    private void updateCandles(TimeFrameStorage timeFrameStorage, PoloniexTrade poloniexTrade, boolean isHistoryTick) {
        // @TODO: single lock for each time frame
        updateCandlesLock.lock();
        try {
            TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
            List<Tick> candles = timeFrameStorage.getCandles();
            ZonedDateTime tradeTime = poloniexTrade.getTradeTime();
            boolean isNewCandleTrade = candles.isEmpty() || !candles.get(candles.size() - 1).inPeriod(tradeTime);
            int builtCandleIndex = candles.size() - 1;
            if (isNewCandleTrade) {
                ////////////////////////////////////////////////////
                if (!candles.isEmpty()) {
                    log.info("Trading on new {} candle", timeFrame);
                    tradesExecutor.submit(() -> {
                        try {
                            onNewCandle(timeFrameStorage, builtCandleIndex, isHistoryTick);
                        } catch (Exception ex) {
                            log.error("Failed to trade on {}", ex);
                        }
                    });
                }
                ////////////////////////////////////////////////////
                candles.add(new Tick(timeFrame.getFrameDuration(), timeFrame.calculateEndTime(tradeTime)));
                if (isHistoryTick) {
                    timeFrameStorage.setHistoryIndex(candles.size() - 1);
                }
                log.info("New {} candle has been created.", timeFrame);
            }
            candles.get(candles.size() - 1).addTrade(Decimal.valueOf(poloniexTrade.getAmount()), Decimal.valueOf(poloniexTrade.getRate()));
        } finally {
            updateCandlesLock.unlock();
        }
    }

    private void onNewCandle(TimeFrameStorage timeFrameStorage, int index, boolean isHistoryTick) {
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        List<Tick> candles = timeFrameStorage.getCandles();
        log.info("Analyzing new {} candle at {}.", timeFrame, index);
        Tick lastCandle = candles.get(index);
        for (PoloniexStrategy poloniexStrategy : timeFrameStorage.getActiveStrategies()) {
            log.debug("Executing strategy '{}' on time series {}", poloniexStrategy.getName(), timeFrame);
            List<PoloniexTradingRecord> tradingRecords = poloniexStrategy.getTradingRecords();
            boolean onceEntered = false;
            for (int trIndex = 0; trIndex < tradingRecords.size(); trIndex++) {
                PoloniexTradingRecord poloniexTradingRecord = tradingRecords.get(trIndex);
                TradingRecord tradingRecord = poloniexTradingRecord.getTradingRecord();
                TradingAction action = realTimeAnalyticsService.analyzeTick(poloniexStrategy.getStrategy(), lastCandle, index, timeFrameStorage.getHistoryIndex(), false, tradingRecord);
                log.debug("Strategy {}/{} trading record {} analytics result {}.", timeFrame, poloniexStrategy.getName(), trIndex, action);
                if (!isHistoryTick && TradingAction.shouldPlaceOrder(action)) {
                    Optional<PoloniexOrder> tradeResultOrder = Optional.empty();
                    boolean canTrade = TradingAction.SHOULD_ENTER != action || !onceEntered;
                    log.debug("Strategy '{}' canTrade/onceEntered flags: {}/{}", poloniexStrategy.getName(), canTrade, onceEntered);
                    if (canTrade) {
                        tradeResultOrder = tradingService.placeOrder(tradingRecord, index, action, properties.getTrade().isRealPrice());
                    }
                    onceEntered |= TradingAction.SHOULD_ENTER == action && tradeResultOrder.isPresent();
                    log.debug("Strategy '{}' onceEntered flag: {}", poloniexStrategy.getName(), onceEntered);
                    tradeResultOrder.ifPresent(poloniexTradingRecord::addPoloniexOrder);
                }
            }
        }
    }

    public List<TimeFrameStorage> getData(CurrencyPair currencyPair) {
        return candles.getOrDefault(CurrencyPair.BTC_ETH, Collections.emptyList());
    }

    public void addTradesHistory(CurrencyPair currency, Set<PoloniexTrade> poloniexTrades) {
        candles.computeIfPresent(currency, (currencyPair, candles) -> {
            updateCandlesLock.lock();
            try {
                candles.forEach(timeFrameStorage -> {
                    TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
                    // @TODO: add history without candles recreation
                    log.info("Clearing {} candles with history for {}", timeFrame, currency);
                    timeFrameStorage.getCandles().clear();
                    log.info("Updating candles with history for {}", currency);
                    poloniexTrades.forEach(poloniexTrade -> updateCandles(timeFrameStorage, poloniexTrade, true));
                });
            } finally {
                updateCandlesLock.unlock();
            }
            return candles;
        });
    }
}