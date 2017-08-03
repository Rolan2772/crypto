package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.ResultTrade;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class TradeCalculator {

    public static boolean canSell(Order entryOrder, BigDecimal sellPrice) {
        BigDecimal expectedProfit = getExpectedProfit(entryOrder, sellPrice);
        log.info("Expected profit: {}, minimum profit: {}", expectedProfit, CalculationsUtils.MIN_PROFIT_PERCENT);
        return expectedProfit.compareTo(CalculationsUtils.MIN_PROFIT_PERCENT) > 0;
    }

    public static BigDecimal getExpectedProfit(Order entryOrder, BigDecimal sellPrice) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal boughtAmount = getAmountWithFee(entryOrder);
        BigDecimal sellGain = getTotal(sellPrice, boughtAmount);
        log.debug("Sell price: {}, entry order: {}, bought amount: {}, sell gain (no fee): {}", sellPrice, entryOrder, boughtAmount, sellGain);
        return CalculationsUtils.divide(sellGain, buySpent);
    }

    public static BigDecimal getAmountWithFee(Order order) {
        BigDecimal result = applyFee(CalculationsUtils.toBigDecimal(order.getAmount()));
        return CalculationsUtils.setCryptoScale(result);
    }

    public static BigDecimal applyFee(BigDecimal value) {
        return value.multiply(CalculationsUtils.AFTER_FEE_PERCENT);
    }

    public static BigDecimal getResultProfit(Order entryOrder, Order exitOrder) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal sellGain = getTotal(exitOrder);
        return sellGain.subtract(buySpent);
    }

    public static BigDecimal getNetResultProfit(Order entryOrder, Order exitOrder) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal sellGain = getTotalWithFee(exitOrder);
        return sellGain.subtract(buySpent);
    }

    public static BigDecimal getResultPercent(Order entryOrder, Order exitOrder) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal sellGain = getTotal(exitOrder);
        return CalculationsUtils.divide(sellGain, buySpent).subtract(BigDecimal.ONE);
    }

    public static BigDecimal getNetResultPercent(Order entryOrder, Order exitOrder) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal sellGain = getTotalWithFee(exitOrder);
        return CalculationsUtils.divide(sellGain, buySpent).subtract(BigDecimal.ONE);
    }

    public static BigDecimal getTotalWithFee(Order order) {
        return CalculationsUtils.setCryptoScale(applyFee(getTotal(order, false)));
    }

    public static BigDecimal getTotal(Order order) {
        return getTotal(order, true);
    }

    public static BigDecimal getTotal(Order order, boolean scale) {
        return getTotal(CalculationsUtils.toBigDecimal(order.getPrice()),
                CalculationsUtils.toBigDecimal(order.getAmount()),
                scale);
    }

    public static BigDecimal getTotal(BigDecimal price, BigDecimal amount) {
        return getTotal(price, amount, true);
    }

        public static BigDecimal getTotal(BigDecimal price, BigDecimal amount, boolean scale) {
        BigDecimal result = price.multiply(amount);
        return scale ? CalculationsUtils.setCryptoScale(result) : result;
    }

    public static BigDecimal getResultRate(List<ResultTrade> resultTrades, BigDecimal defaultRate) {
        BigDecimal rate = resultTrades.isEmpty() ? resultTrades.get(0).getRate() : defaultRate;
        if (resultTrades.size() > 1) {
            BigDecimal spent = resultTrades.stream()
                    .map(ResultTrade::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rate = CalculationsUtils.divide(spent, getResultAmount(resultTrades, BigDecimal.ONE));
        }
        return rate;
    }

    public static BigDecimal getResultAmount(List<ResultTrade> resultTrades, BigDecimal defaultAmount) {
        return resultTrades.stream()
                .map(ResultTrade::getAmount)
                .reduce(defaultAmount, BigDecimal::add);
    }
}
