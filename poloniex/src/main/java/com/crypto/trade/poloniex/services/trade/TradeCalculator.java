package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.ResultTrade;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class TradeCalculator {

    // @TODO: smth with rounding
    // @TODO: use 1% no fee need
    public boolean canSell(Order entryOrder, BigDecimal sellPrice) {
        BigDecimal expectedProfit = getExpectedProfit(entryOrder, sellPrice);
        log.info("Expected profit: {}, minimum profit: {}", expectedProfit, CalculationsUtils.MIN_PROFIT_PERCENT);
        return expectedProfit.compareTo(CalculationsUtils.MIN_PROFIT_PERCENT) > 0;
    }

    public BigDecimal getExpectedProfit(Order entryOrder, BigDecimal sellPrice) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal boughtAmount = getAmountWithFee(entryOrder);
        BigDecimal sellGain = applyFee(getTotal(sellPrice, boughtAmount));
        log.debug("Sell price: {}, entry order: {}, bought amount: {}, sell gain: {}", sellPrice, entryOrder, boughtAmount, sellGain);
        return CalculationsUtils.divide(sellGain, buySpent);
    }

    public BigDecimal getAmountWithFee(Order order) {
        return applyFee(CalculationsUtils.toBigDecimal(order.getAmount()));
    }

    public BigDecimal applyFee(BigDecimal value) {
        BigDecimal afterFee = value.multiply(CalculationsUtils.AFTER_FEE_PERCENT);
        return CalculationsUtils.setCryptoScale(afterFee);
    }

    public BigDecimal getResultProfit(Order entryOrder, Order exitOrder) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal sellGain = applyFee(getTotal(exitOrder));
        return sellGain.subtract(buySpent);
    }

    public BigDecimal getResultPercent(Order entryOrder, Order exitOrder) {
        BigDecimal buySpent = getTotal(entryOrder);
        BigDecimal sellGain = applyFee(getTotal(exitOrder));
        return CalculationsUtils.divide(sellGain, buySpent);
    }

    public BigDecimal getTotal(Order order) {
        return getTotal(CalculationsUtils.toBigDecimal(order.getPrice()),
                CalculationsUtils.toBigDecimal(order.getAmount()));
    }

    public BigDecimal getTotalWithFee(Order order) {
        return getTotal(CalculationsUtils.toBigDecimal(order.getPrice()),
                getAmountWithFee(order));
    }

    public BigDecimal getTotal(BigDecimal price, BigDecimal amount) {
        return CalculationsUtils.setCryptoScale(price.multiply(amount));
    }

    // @TODO: total/amount != amount*rate
    public BigDecimal getResultRate(List<ResultTrade> resultTrades, BigDecimal defaultRate) {
        BigDecimal spent = resultTrades.stream()
                .map(ResultTrade::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = CalculationsUtils.divide(spent, getResultAmount(resultTrades, BigDecimal.ONE));
        return rate.compareTo(BigDecimal.ZERO) == 0 ? defaultRate : rate;
    }

    public BigDecimal getResultAmount(List<ResultTrade> resultTrades, BigDecimal defaultAmount) {
        return resultTrades.stream()
                .map(ResultTrade::getAmount)
                .reduce(defaultAmount, BigDecimal::add);
    }
}
