package com.crypto.trade.poloniex.storage;

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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CandlesStorage {

    @Autowired
    private AnalyticsService realTimeAnalyticsService;
    @Autowired
    private TradingService tradingService;

    private ReentrantLock updateCandlesLock = new ReentrantLock();

    @Getter
    private ConcurrentMap<CurrencyPair, List<TimeFrameStorage>> candles = new ConcurrentHashMap<>();

    public List<PoloniexStrategy> getActiveStrategies(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return candles.getOrDefault(currencyPair, Collections.emptyList())
                .stream()
                .filter(timeFrameStorage -> timeFrameStorage.getTimeFrame() == timeFrame)
                .findFirst()
                .get()
                .getActiveStrategies();
    }

    public void addTrade(CurrencyPair currency, PoloniexTrade poloniexTrade) {
        candles.computeIfPresent(currency, (currencyPair, candles) -> {
            // @TODO: implement update in new thread
            candles.forEach(timeFrameStorage -> updateCandles(timeFrameStorage, poloniexTrade, false));
            return candles;
        });

    }

    private void updateCandles(TimeFrameStorage timeFrameStorage, PoloniexTrade poloniexTrade, boolean isHistoryTick) {
        updateCandlesLock.lock();
        try {
            TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
            List<Tick> candles = timeFrameStorage.getCandles();
            ZonedDateTime tradeTime = poloniexTrade.getTradeTime();
            boolean isNewCandleTrade = candles.isEmpty() || !candles.get(candles.size() - 1).inPeriod(tradeTime);
            if (isNewCandleTrade) {
                ////////////////////////////////////////////////////
                onNewCandle(timeFrameStorage, isHistoryTick);
                ////////////////////////////////////////////////////
                candles.add(new Tick(timeFrame.getFrameDuration(), timeFrame.calculateEndTime(tradeTime)));
                log.info("{} candle has been built.", timeFrame);
            }
            candles.get(candles.size() - 1).addTrade(Decimal.valueOf(poloniexTrade.getAmount()), Decimal.valueOf(poloniexTrade.getRate()));
        } finally {
            updateCandlesLock.unlock();
        }
    }

    private void onNewCandle(TimeFrameStorage timeFrameStorage, boolean historyTick) {
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        List<Tick> candles = timeFrameStorage.getCandles();
        log.info("Analyzing new {} candle.", timeFrame);
        int index = candles.size() - 1;
        Tick lastCandle = candles.get(index);
        for (PoloniexStrategy poloniexStrategy : timeFrameStorage.getActiveStrategies()) {
            log.debug("Executing strategy '{}' on time series {}", poloniexStrategy.getName(), candles);
            List<PoloniexTradingRecord> tradingRecords = poloniexStrategy.getTradingRecords();
            // @TODO: only add new trading record if current is entered
            for (int trIndex = 0; trIndex < tradingRecords.size(); trIndex++) {
                PoloniexTradingRecord poloniexTradingRecord = tradingRecords.get(trIndex);
                TradingRecord tradingRecord = poloniexTradingRecord.getTradingRecord();
                TradingAction action = realTimeAnalyticsService.analyzeTick(poloniexStrategy.getStrategy(), lastCandle, index, tradingRecord);
                log.debug("Strategy {} trading record {} analytics result {}.", poloniexStrategy.getName(), trIndex, action);
                // @TODO: separate buys from sells
                if (!historyTick && TradingAction.shouldPlaceOrder(action)) {
                    // @TODO: each HTTP request will slow strategies and trading records
                    Optional<PoloniexOrder> poloniexOrder = tradingService.placeOrder(tradingRecord, index, action, true);
                    poloniexOrder.ifPresent(poloniexTradingRecord::addPoloniexOrder);
                }
            }
        }
    }

    public void addTradesHistory(CurrencyPair currency, List<PoloniexTrade> poloniexTrades) {
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