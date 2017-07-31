package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.ResultTrade;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class TradeCalculator {

    public BigDecimal getAmountAfterFee(Order order) {
        BigDecimal afterFee = CalculationsUtils.toBigDecimal(order.getAmount()).multiply(CalculationsUtils.AFTER_FEE_PERCENT);
        return CalculationsUtils.setCryptoScale(afterFee);
    }

    public BigDecimal getResultAmount(List<ResultTrade> resultTrades, BigDecimal defaultAmount) {
        return resultTrades.stream()
                .map(ResultTrade::getAmount)
                .reduce(defaultAmount, BigDecimal::add);
    }

    public BigDecimal getResultRate(List<ResultTrade> resultTrades, BigDecimal defaultRate) {
        BigDecimal spent = resultTrades.stream()
                .map(ResultTrade::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = CalculationsUtils.divide(spent, getResultAmount(resultTrades, BigDecimal.ONE));
        return rate.compareTo(BigDecimal.ZERO) == 0 ? defaultRate : rate;
    }

    public boolean canSell(Order entryOrder, BigDecimal sellRate) {
        // Profit calculations
        BigDecimal openPrice = CalculationsUtils.toBigDecimal(entryOrder.getPrice());
        BigDecimal buyAmount = CalculationsUtils.toBigDecimal(entryOrder.getAmount());
        BigDecimal buySpent = openPrice.multiply(buyAmount);

        BigDecimal buyAmountAfterFee = getAmountAfterFee(entryOrder);
        BigDecimal sellGain = sellRate.multiply(buyAmountAfterFee).multiply(CalculationsUtils.AFTER_FEE_PERCENT);
        log.debug("Open price = {}, last price = {}, buy amount = {}, but spent = {}, sell gain = {}", openPrice, sellRate, buyAmount, buySpent, sellGain);
        BigDecimal diff = CalculationsUtils.divide(sellGain, buySpent);
        log.info("Expected/required SELL profit {}/{}", diff, CalculationsUtils.MIN_PROFIT_PERCENT);

        return diff.compareTo(CalculationsUtils.MIN_PROFIT_PERCENT) > 0;
    }
}
