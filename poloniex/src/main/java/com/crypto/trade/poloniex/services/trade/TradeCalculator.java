package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.ResultTrade;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class TradeCalculator {
    
    public static boolean canExit(Order entryOrder, BigDecimal exitPrice) {
        BigDecimal expectedProfit = getExpectedProfit(entryOrder, exitPrice);
        log.info("Expected profit: {}, minimum profit: {}", expectedProfit, CalculationsUtils.MIN_PROFIT_PERCENT);
        return expectedProfit.compareTo(CalculationsUtils.MIN_PROFIT_PERCENT) >= 0;
    }

    public static BigDecimal getExpectedProfit(Order entryOrder, BigDecimal exitPrice) {
        BigDecimal entrySpent = getEntrySpent(entryOrder);
        BigDecimal exitGain = getGrossExitGain(entryOrder, exitPrice);
        log.debug("Sell price: {}, entry order: {}, bury spent: {}, sell gain: {}", exitPrice, entryOrder, entrySpent, exitGain);
        return CalculationsUtils.divide(exitGain, entrySpent);
    }

    public static BigDecimal getEntryAmount(BigDecimal volume, BigDecimal rate, Order.OrderType direction) {
        return direction == Order.OrderType.BUY
                ? CalculationsUtils.divide(volume, rate)
                : volume;
    }

    public static BigDecimal getExitAmount(Order entryOrder, BigDecimal rate) {
        return entryOrder.getType() == Order.OrderType.BUY
                ? subtractFee(CalculationsUtils.toBigDecimal(entryOrder.getAmount()))
                : CalculationsUtils.divide(subtractFee(getTotal(entryOrder)), rate);
    }

    public static BigDecimal subtractFee(BigDecimal value) {
        BigDecimal fee = CalculationsUtils.setCryptoScale(value.multiply(CalculationsUtils.FEE_PERCENT));
        return value.subtract(fee);
    }

    public static BigDecimal getEntrySpent(Order entryOrder) {
        return entryOrder.getType() == Order.OrderType.BUY
                ? getTotal(entryOrder)
                : CalculationsUtils.toBigDecimal(entryOrder.getAmount());
    }

    public static BigDecimal getGrossExitGain(Order entryOrder, Order exitOrder) {
        BigDecimal exitPrice = CalculationsUtils.toBigDecimal(exitOrder.getPrice());
        BigDecimal entryAmount = CalculationsUtils.toBigDecimal(entryOrder.getAmount());
        return entryOrder.getType() == Order.OrderType.BUY
                ? getTotal(exitPrice, entryAmount)
                : CalculationsUtils.setCryptoScale(CalculationsUtils.toBigDecimal(exitOrder.getAmount()));
    }

    public static BigDecimal getGrossExitGain(Order entryOrder, BigDecimal exitPrice) {
        BigDecimal entryAmount = CalculationsUtils.toBigDecimal(entryOrder.getAmount());
        return entryOrder.getType() == Order.OrderType.BUY
                ? getTotal(exitPrice, entryAmount)
                : CalculationsUtils.divide(getTotal(entryOrder), exitPrice);
    }

    public static BigDecimal getNetExitGain(Order entryOrder, Order exitOrder) {
        return entryOrder.getType() == Order.OrderType.BUY
                ? subtractFee(getTotal(exitOrder))
                : subtractFee(CalculationsUtils.toBigDecimal(exitOrder.getAmount()));
    }

    public static BigDecimal getTotalWithFee(Order order) {
        return subtractFee(getTotal(order));
    }

    public static BigDecimal getTotal(Order order) {
        return getTotal(CalculationsUtils.toBigDecimal(order.getPrice()),
                CalculationsUtils.toBigDecimal(order.getAmount()));
    }

    public static BigDecimal getTotal(BigDecimal price, BigDecimal amount) {
        BigDecimal result = price.multiply(amount);
        return CalculationsUtils.setCryptoScale(result);
    }

    public static BigDecimal getResultRate(List<ResultTrade> resultTrades, BigDecimal defaultRate) {
        BigDecimal rate = resultTrades.isEmpty() ? defaultRate : resultTrades.get(0).getRate();
        if (resultTrades.size() > 1) {
            BigDecimal spent = resultTrades.stream()
                    .map(resultTrade -> resultTrade.getAmount().multiply(resultTrade.getRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rate = CalculationsUtils.divide(spent, getResultAmount(resultTrades, BigDecimal.ONE));
        }
        return rate;
    }

    public static BigDecimal getResultAmount(List<ResultTrade> resultTrades, BigDecimal defaultAmount) {
        return resultTrades.isEmpty()
                ? defaultAmount
                : resultTrades.stream().map(ResultTrade::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
