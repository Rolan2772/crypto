package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import com.crypto.trade.poloniex.services.utils.DateTimeUtils;
import com.crypto.trade.poloniex.services.utils.ExportUtils;
import com.crypto.trade.poloniex.storage.PoloniexOrder;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.analysis.criteria.LinearTransactionCostCriterion;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.AbstractMap;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExportHelper {

    @Qualifier("historyAnalyticsService")
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private TradeCalculator tradeCalculator;

    public String createStrategiesHeaders(List<PoloniexTradingRecord> tradingRecords, String type) {
        return tradingRecords.stream()
                .map(tradingRecord -> ExportUtils.getTradingRecordName(tradingRecord) + "-" + type)
                .collect(Collectors.joining(","));
    }

    public String convertCandle(TimeSeries timeSeries, int index) {
        Tick tick = timeSeries.getTick(index);
        return Stream.of(tick.getTimePeriod(),
                DateTimeUtils.format(tick.getBeginTime()),
                DateTimeUtils.format(tick.getEndTime()),
                tick.getOpenPrice(),
                tick.getClosePrice(),
                tick.getMaxPrice(),
                tick.getMinPrice(),
                tick.getAmount(),
                tick.getVolume())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertIndicators(TimeSeries timeSeries, List<Indicator<?>> indicators, int index) {
        String closeTime = DateTimeUtils.format(timeSeries.getTick(index).getBeginTime());
        String values = indicators.stream()
                .map(indicator -> indicator.getValue(index))
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return closeTime + "," + values;
    }

    public String createHistoryTradesAnalytics(List<PoloniexStrategy> strategies, TimeSeries timeSeries, int index, int historyIndex) {
        Tick tick = timeSeries.getTick(index);
        return strategies.stream()
                .flatMap(strategy -> strategy.getTradingRecords()
                        .stream()
                        .map(tradingRecord -> analyticsService.analyzeTick(strategy.getStrategy(),
                                tick,
                                index,
                                historyIndex,
                                false,
                                tradingRecord.getTradingRecord())))
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertRealTrades(List<PoloniexTradingRecord> tradingRecords, int index) {
        return tradingRecords.stream()
                .map(tradingRecord -> tradingRecord.getOrders()
                        .stream()
                        .filter(order -> index == order.getIndex())
                        .map(PoloniexOrder::getAction)
                        .findFirst()
                        .orElse(TradingAction.NO_ACTION))
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String createResultAnalytics(TimeSeries candles, List<PoloniexStrategy> strategies) {
        StringBuilder sb = new StringBuilder("name,tradesCount,profit,profitWithFee\n");
        strategies.stream()
                .map(strategy -> new AbstractMap.SimpleEntry<>(strategy.getTradeVolume(), strategy.getTradingRecords()))
                .forEach(entry -> {
                    entry.getValue().forEach(record -> {
                        TradingRecord tr = record.getTradingRecord();
                        sb.append(ExportUtils.getTradingRecordName(record))
                                .append(",")
                                .append(tr.getTradeCount())
                                .append(",")
                                .append(new TotalProfitCriterion().calculate(candles, tr))
                                .append(",")
                                .append(new LinearTransactionCostCriterion(entry.getKey().doubleValue(), CalculationsUtils.FEE_PERCENT.doubleValue()).calculate(candles, tr))
                                .append('\n');
                    });
                });
        return sb.toString();

    }

    public List<PoloniexStrategy> createTradingRecordsCopy(List<PoloniexStrategy> strategies) {
        return strategies.stream()
                .map(strategy -> {
                    List<PoloniexTradingRecord> tradingRecords = strategy.getTradingRecords().stream()
                            .map(tr -> new PoloniexTradingRecord(tr.getId(), tr.getStrategyName(), new TradingRecord()))
                            .collect(Collectors.toList());
                    return new PoloniexStrategy(strategy, tradingRecords);
                }).collect(Collectors.toList());
    }

    public String convertOrder(String name, PoloniexOrder poloniexOrder) {
        Order sourceOrder = poloniexOrder.getSourceOrder();
        return Stream.of(name,
                poloniexOrder.getOrderId(),
                DateTimeUtils.format(poloniexOrder.getTradeTime()),
                poloniexOrder.getIndex(),
                sourceOrder.getPrice(),
                sourceOrder.getAmount(),
                poloniexOrder.getFee(),
                tradeCalculator.getAmountWithFee(sourceOrder),
                sourceOrder.isBuy() ? tradeCalculator.getTotal(sourceOrder) : tradeCalculator.getTotalWithFee(sourceOrder),
                sourceOrder.getType())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertTradingRecordProfit(PoloniexTradingRecord tradingRecord) {
        StringBuilder sb = new StringBuilder(ExportUtils.getTradingRecordName(tradingRecord));
        ProfitAccumulator profitAccumulator = getTradingRecordProfit(tradingRecord);
        sb.append(",")
                .append(profitAccumulator.getCcyProfit())
                .append(",")
                .append(profitAccumulator.getPercentageProfit());
        return sb.toString();
    }

    public String convertStrategyProfit(PoloniexStrategy strategy) {
        StringBuilder sb = new StringBuilder();
        ProfitAccumulator strategyProfit = strategy.getTradingRecords()
                .stream()
                .map(this::getTradingRecordProfit)
                .reduce(new ProfitAccumulator(), profitAccumulator());
        sb.append(strategy.getName())
                .append(",")
                .append(strategyProfit.getCcyProfit())
                .append(",")
                .append(strategyProfit.getPercentageProfit());
        return sb.toString();
    }

    public String convertTotalProfit(TimeFrameStorage timeFrameStorage) {
        StringBuilder sb = new StringBuilder();
        ProfitAccumulator totalProfit = timeFrameStorage.getAllTradingRecords()
                .stream()
                .map(this::getTradingRecordProfit)
                .reduce(new ProfitAccumulator(), profitAccumulator());

        sb.append("Total")
                .append(",")
                .append(totalProfit.getCcyProfit())
                .append(",")
                .append(totalProfit.getPercentageProfit());
        return sb.toString();
    }

    private BinaryOperator<ProfitAccumulator> profitAccumulator() {
        return (profitAccumulator1, profitAccumulator2) -> {
            profitAccumulator1.addCcyProfit(profitAccumulator2.getCcyProfit());
            profitAccumulator1.addPercentageProfit(profitAccumulator2.getPercentageProfit());
            return profitAccumulator1;
        };
    }

    private ProfitAccumulator getTradingRecordProfit(PoloniexTradingRecord tradingRecord) {
        ProfitAccumulator profit = new ProfitAccumulator();
        List<PoloniexOrder> orders = tradingRecord.getOrders();
        if (orders.size() > 1) {
            for (int index = 1; index < orders.size(); index += 2) {
                PoloniexOrder entryOrder = orders.get(index - 1);
                PoloniexOrder exitOrder = orders.get(index);
                profit.addCcyProfit(tradeCalculator.getResultProfit(entryOrder.getSourceOrder(), exitOrder.getSourceOrder()));
                profit.addPercentageProfit(tradeCalculator.getResultPercent(entryOrder.getSourceOrder(), exitOrder.getSourceOrder()));
            }
        }
        return profit;
    }
}