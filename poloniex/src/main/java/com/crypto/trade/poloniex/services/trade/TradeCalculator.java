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
        BigDecimal sellGain = getTotal(sellPrice, CalculationsUtils.toBigDecimal(entryOrder.getAmount()));
        log.debug("Sell price: {}, entry order: {}, bury spent: {}, sell gain: {}", sellPrice, entryOrder, buySpent, sellGain);
        return CalculationsUtils.divide(sellGain, buySpent);
    }

    public static BigDecimal getAmountWithFee(Order order) {
        return subtractFee(CalculationsUtils.toBigDecimal(order.getAmount()));
    }

    public static BigDecimal subtractFee(BigDecimal value) {
        BigDecimal fee = CalculationsUtils.setCryptoScale(value.multiply(CalculationsUtils.FEE_PERCENT));
        return value.subtract(fee);
    }

    public static BigDecimal getGrossSellGain(Order entryOrder, Order exitOrder) {
        return getTotal(CalculationsUtils.toBigDecimal(exitOrder.getPrice()),
                CalculationsUtils.toBigDecimal(entryOrder.getAmount()));
    }

    public static BigDecimal getBuySpent(Order entryOrder) {
        return getTotal(entryOrder);
    }

    public static BigDecimal getNetSellGain(Order exitOrder) {
        return subtractFee(getTotal(exitOrder));
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
                    .map(ResultTrade::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rate = CalculationsUtils.divide(spent, getResultAmount(resultTrades, BigDecimal.ONE));
        }
        return rate;
    }

    public static BigDecimal getResultAmount(List<ResultTrade> resultTrades, BigDecimal defaultAmount) {
        BigDecimal result = resultTrades.isEmpty() ? defaultAmount : resultTrades.get(0).getAmount();
        if (resultTrades.size() > 1) {
            result = resultTrades.stream()
                    .map(ResultTrade::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return result;
    }
}
