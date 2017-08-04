package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.trade.TradingService;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Value
@Slf4j
public class NewCandleSupplier implements Supplier<Tick> {

    private TimeFrameStorage timeFrameStorage;
    private ZonedDateTime tradeTime;
    private ThreadPoolTaskExecutor strategyExecutor;
    private AnalyticsService realTimeAnalyticsService;
    private TradingService tradingService;
    private boolean isRealPrice;
    private boolean isHistoryTick;

    @Override
    public Tick get() {
        log.info("No candle found for {} trade.", tradeTime.toLocalDateTime());
        List<Tick> candles = timeFrameStorage.getCandles();
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        int lastIndex = candles.size() - 1;

        if (isHistoryTick) {
            timeFrameStorage.setHistoryIndex(lastIndex + 1);
        } else {
            trade(timeFrameStorage, lastIndex);
        }

        // @TODO: check missed candles and indicators when no ticks
        Tick newCandle = new Tick(timeFrame.getFrameDuration(), timeFrame.calculateEndTime(tradeTime));
        candles.add(newCandle);
        log.info("New {} candle {} - {} with index {} has been created.", timeFrame, newCandle.getBeginTime().toLocalDateTime(), newCandle.getEndTime().toLocalDateTime(), candles.size() - 1);
        return newCandle;
    }

    private void trade(TimeFrameStorage timeFrameStorage, int index) {
        List<Tick> candles = timeFrameStorage.getCandles();
        if (!candles.isEmpty()) {
            TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
            log.info("Trading on built {} candle at index {}", timeFrame, index);
            strategyExecutor.submit(() -> {
                try {
                    onNewCandle(timeFrameStorage, index);
                } catch (Exception ex) {
                    log.error("Failed to trade on " + timeFrame + " at index " + index, ex);
                }
            });
        }
    }

    private void onNewCandle(TimeFrameStorage timeFrameStorage, int index) {
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        log.info("Analyzing new {} candle at {}.", timeFrame, index);
        Tick builtCandle = timeFrameStorage.getCandles().get(index);
        for (PoloniexStrategy poloniexStrategy : timeFrameStorage.getActiveStrategies()) {
            log.debug("Executing strategy '{}' on time series {}", poloniexStrategy.getName(), timeFrame);
            List<PoloniexTradingRecord> tradingRecords = poloniexStrategy.getTradingRecords();
            boolean onceEntered = false;
            // @TODO: {"error":"Unable to fill order completely."} with 20 trading records cause big delay (a lot of new candles olready been created)
            for (int trIndex = 0; trIndex < tradingRecords.size(); trIndex++) {
                PoloniexTradingRecord poloniexTradingRecord = tradingRecords.get(trIndex);
                TradingRecord tradingRecord = poloniexTradingRecord.getTradingRecord();
                TradingAction action = realTimeAnalyticsService.analyzeTick(poloniexStrategy.getStrategy(), builtCandle, index, timeFrameStorage.getHistoryIndex(), false, tradingRecord);
                log.debug("Strategy {}/{} trading record {} analytics result {}, processing: {}.", timeFrame, poloniexStrategy.getName(), trIndex, action, poloniexTradingRecord.getProcessing().get());
                if (TradingAction.shouldPlaceOrder(action)) {
                    try {
                        boolean canTrade = (TradingAction.SHOULD_ENTER != action || !onceEntered) && poloniexTradingRecord.getProcessing().compareAndSet(false, true);
                        log.debug("Strategy '{}' canTrade: {}, onceEntered: {}, processing: {}", poloniexStrategy.getName(), canTrade, onceEntered, poloniexTradingRecord.getProcessing().get());
                        Optional<PoloniexOrder> resultOrder = Optional.empty();
                        if (canTrade) {
                            resultOrder = tradingService.placeOrder(tradingRecord, index, poloniexStrategy.getTradeVolume(), isRealPrice);
                            poloniexTradingRecord.setProcessed();
                        }
                        onceEntered |= TradingAction.SHOULD_ENTER == action && resultOrder.isPresent();
                        log.debug("Strategy '{}' onceEntered flag: {}", poloniexStrategy.getName(), onceEntered);
                        resultOrder.ifPresent(poloniexTradingRecord::addPoloniexOrder);
                    } finally {
                        poloniexTradingRecord.setProcessed();
                        log.debug("Trading record processing: {}", poloniexTradingRecord.getProcessing().get());
                    }
                }
            }
        }
    }
}
