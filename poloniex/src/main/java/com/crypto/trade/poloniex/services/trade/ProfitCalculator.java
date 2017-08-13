package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BinaryOperator;

public class ProfitCalculator {

    private BinaryOperator<TradeResult> tradesAccumulator = (t1, t2) -> {
        t1.accumulate(t2.getBuySpent(),
                t2.getNetSellGain(),
                t2.getGrossSellGain());
        t1.accumulateVolume(t2.getVolume());
        t1.accumulateTradesCount(t2.getTradesCount());
        return t1;
    };

    public BigDecimal getNetPercent(TradeResult tradeResult) {
        BigDecimal netProfit = getNetProfit(tradeResult);
        return tradeResult.getVolume().compareTo(BigDecimal.ZERO) != 0
                ? CalculationsUtils.divide(netProfit, tradeResult.getVolume())
                : BigDecimal.ZERO;
    }

    public BigDecimal getGrossPercent(TradeResult tradeResult) {
        BigDecimal grossProfit = getGrossProfit(tradeResult);
        return tradeResult.getVolume().compareTo(BigDecimal.ZERO) != 0
                ? CalculationsUtils.divide(grossProfit, tradeResult.getVolume())
                : grossProfit;
    }

    public BigDecimal getNetProfit(TradeResult tradeResult) {
        return tradeResult.getNetSellGain().subtract(tradeResult.getBuySpent());
    }

    public BigDecimal getGrossProfit(TradeResult tradeResult) {
        return tradeResult.getGrossSellGain().subtract(tradeResult.getBuySpent());
    }

    public TradeResult getTotalTradeResult(List<PoloniexStrategy> strategies) {
        return strategies.stream()
                .map(this::getStrategyTradeResult)
                .reduce(new TradeResult(), tradesAccumulator);
    }

    public TradeResult getStrategyTradeResult(PoloniexStrategy strategy) {
        return strategy.getTradingRecords()
                .stream()
                .map(tradingRecord -> getTradingRecordTradeResult(tradingRecord, strategy.getTradeVolume()))
                .reduce(new TradeResult(), tradesAccumulator);
    }

    public TradeResult getTradingRecordTradeResult(PoloniexTradingRecord tradingRecord, BigDecimal volume) {
        TradeResult tradeResult = new TradeResult();
        tradeResult.accumulateVolume(volume);
        tradeResult.accumulateTradesCount(tradingRecord.getTradingRecord().getTradeCount());
        List<Trade> trades = tradingRecord.getTradingRecord().getTrades();
        for (Trade trade : trades) {
            if (trade.isClosed()) {
                Order entryOrder = trade.getEntry();
                Order exitOrder = trade.getExit();
                tradeResult.accumulate(TradeCalculator.getBuySpent(entryOrder),
                        TradeCalculator.getNetSellGain(exitOrder),
                        TradeCalculator.getGrossSellGain(entryOrder, exitOrder));
            }
        }
        /*List<PoloniexOrder> orders = tradingRecord.getOrders();
        if (orders.size() > 1) {
            for (int index = 1; index < orders.size(); index += 2) {
                Order entryOrder = orders.get(index - 1).getSourceOrder();
                Order exitOrder = orders.get(index).getSourceOrder();
                tradeResult.accumulate(TradeCalculator.getBuySpent(entryOrder),
                        TradeCalculator.getNetSellGain(exitOrder),
                        TradeCalculator.getGrossSellGain(entryOrder, exitOrder));
            }
        }*/
        return tradeResult;
    }
}
