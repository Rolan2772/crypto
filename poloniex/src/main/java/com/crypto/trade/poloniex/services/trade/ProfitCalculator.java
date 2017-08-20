package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class ProfitCalculator {

    private BinaryOperator<TradeResult> tradesResultAccumulator = (t1, t2) -> {
        if (t1.getDirection() != t2.getDirection()) {
            throw new IllegalStateException("Trade result from opposite direction cannot be aggreagted");
        }
        return new TradeResult(t1.getEntrySpent().add(t2.getEntrySpent()),
                t1.getNetExitGain().add(t2.getNetExitGain()),
                t1.getGrossExitGain().add(t2.getGrossExitGain()),
                t1.getDirection(),
                t1.getVolume().add(t2.getVolume()),
                t1.getTradesCount() + t2.getTradesCount());
    };

    public BigDecimal getNetPercent(TradeResult tradeResult) {
        BigDecimal netProfit = getNetProfit(tradeResult);
        return hasVolume(tradeResult)
                ? CalculationsUtils.divide(netProfit, tradeResult.getVolume())
                : BigDecimal.ZERO;
    }

    private boolean hasVolume(TradeResult tradeResult) {
        return tradeResult.getVolume().compareTo(BigDecimal.ZERO) != 0;
    }

    public BigDecimal getGrossPercent(TradeResult tradeResult) {
        BigDecimal grossProfit = getGrossProfit(tradeResult);
        return hasVolume(tradeResult)
                ? CalculationsUtils.divide(grossProfit, tradeResult.getVolume())
                : grossProfit;
    }

    public BigDecimal getNetProfit(TradeResult tradeResult) {
        return tradeResult.getNetExitGain().subtract(tradeResult.getEntrySpent());
    }

    public BigDecimal getGrossProfit(TradeResult tradeResult) {
        return tradeResult.getGrossExitGain().subtract(tradeResult.getEntrySpent());
    }

    public TradeResult getTotalTradeResult(List<PoloniexStrategy> strategies, Order.OrderType direction) {
        return strategies.stream()
                .filter(strategy -> strategy.getDirection() == direction)
                .map(this::getStrategyTradeResult)
                .reduce(new TradeResult(direction), tradesResultAccumulator);
    }

    public TradeResult getStrategyTradeResult(PoloniexStrategy strategy) {
        return strategy.getTradingRecords()
                .stream()
                .map(tradingRecord -> getTradingRecordProfit(tradingRecord, strategy.getDirection(), strategy.getTradeVolume()))
                .reduce(new TradeResult(strategy.getDirection()), tradesResultAccumulator);
    }

    public TradeResult getTradingRecordProfit(PoloniexTradingRecord poloniexTradingRecord, Order.OrderType direction, BigDecimal volume) {
        return poloniexTradingRecord.getTradingRecord()
                .getTrades()
                .stream()
                .filter(Trade::isClosed)
                .map(tradeResultMapper(direction))
                .reduce(new TradeResult(direction, volume), tradesResultAccumulator);

    }

    private Function<Trade, TradeResult> tradeResultMapper(Order.OrderType direction) {
        return trade -> {
            Order entryOrder = trade.getEntry();
            Order exitOrder = trade.getExit();
            return new TradeResult(TradeCalculator.getEntrySpent(entryOrder),
                    TradeCalculator.getNetExitGain(entryOrder, exitOrder),
                    TradeCalculator.getGrossExitGain(entryOrder, exitOrder),
                    direction,
                    BigDecimal.ZERO,
                    1);
        };
    }
}
