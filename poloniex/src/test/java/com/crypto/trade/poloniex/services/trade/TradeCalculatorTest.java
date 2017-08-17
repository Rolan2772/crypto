package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.ResultTrade;
import com.crypto.trade.utils.TestOrderUtils;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TradeCalculatorTest {

    @Test
    public void getTotal() {
        assertEquals(BigDecimal.valueOf(0.00099999),
                TradeCalculator.getTotal(BigDecimal.valueOf(0.07042652), BigDecimal.valueOf(0.01419919)));
    }

    @Test
    public void orderTotal() {
        Order order = TestOrderUtils.createEntryOrder(Decimal.valueOf("0.08242652"), Decimal.valueOf("0.12132017"));
        assertEquals(BigDecimal.valueOf(0.00999999), TradeCalculator.getTotal(order));
    }

    @Test
    public void orderTotalWithFee() {
        Order order = TestOrderUtils.createEntryOrder(Decimal.valueOf("0.08242652"), Decimal.valueOf("0.12132017"));
        assertEquals(new BigDecimal("0.00997500"), TradeCalculator.getTotalWithFee(order));
    }

    @Test
    public void subtractFee() {
        assertEquals(new BigDecimal("0.01005830"), TradeCalculator.subtractFee(BigDecimal.valueOf(0.0100835)));
        assertEquals(BigDecimal.valueOf(0.00000001), TradeCalculator.subtractFee(BigDecimal.valueOf(0.00000001)));
        assertEquals(BigDecimal.valueOf(0.00009975), TradeCalculator.subtractFee(BigDecimal.valueOf(0.0001000)));
    }

    @Test
    public void amount() {
        assertEquals(BigDecimal.valueOf(0.11237875),
                TradeCalculator.getAmount(BigDecimal.valueOf(0.01011364), BigDecimal.valueOf(0.08999601)));
    }

    @Test
    public void amountWithFee() {
        Order order = TestOrderUtils.createEntryOrder(Decimal.valueOf("0.08999601"), Decimal.valueOf("0.11237875"));
        assertEquals(BigDecimal.valueOf(0.11209781),
                TradeCalculator.getAmountWithFee(order));
    }

    @Test
    public void buySpent() {
        Order entryOrder = TestOrderUtils.createEntryOrder(Decimal.valueOf("0.08242652"), Decimal.valueOf("0.12132017"));
        assertEquals(BigDecimal.valueOf(0.00999999), TradeCalculator.getBuySpent(entryOrder));
    }

    @Test
    public void netSellGain() {
        Order exitOrder = TestOrderUtils.createEntryOrder(Order.OrderType.SELL,
                Decimal.valueOf("0.09199601"),
                Decimal.valueOf("0.11209781"));

        assertEquals(BigDecimal.valueOf(0.01028677), TradeCalculator.getNetSellGain(exitOrder));
    }

    @Test
    public void grossSellGain() {
        Order entryOrder = TestOrderUtils.createEntryOrder(Order.OrderType.BUY,
                Decimal.valueOf("0.09000543"),
                Decimal.valueOf("0.11237805"));
        Order exitOrder = TestOrderUtils.createEntryOrder(Order.OrderType.SELL,
                Decimal.valueOf("0.09199601"),
                Decimal.valueOf("0.11209781"));

        assertEquals(BigDecimal.valueOf(0.01033833), TradeCalculator.getGrossSellGain(entryOrder, exitOrder));
    }

    @Test
    public void noTradesRate() {
        assertEquals(BigDecimal.ZERO, TradeCalculator.getResultRate(Collections.emptyList(), BigDecimal.ZERO));
        assertEquals(BigDecimal.ONE, TradeCalculator.getResultRate(Collections.emptyList(), BigDecimal.ONE));
    }

    @Test
    public void oneTradeRate() {
        BigDecimal rate = BigDecimal.valueOf(0.09199601);
        List<ResultTrade> resultTrades = Collections.singletonList(ResultTrade.builder()
                .rate(rate)
                .amount(BigDecimal.valueOf(0.11209781))
                .total(BigDecimal.valueOf(0.01031255))
                .build());

        assertEquals(rate, TradeCalculator.getResultRate(resultTrades, BigDecimal.ZERO));
    }

    @Test
    public void manyTradesRate() {
        List<ResultTrade> resultTrades = Arrays.asList(ResultTrade.builder()
                        .rate(BigDecimal.valueOf(0.09199601))
                        .amount(BigDecimal.valueOf(0.11209781))
                        .total(BigDecimal.valueOf(0.01031255))
                        .build(),
                ResultTrade.builder()
                        .rate(BigDecimal.valueOf(0.0854935))
                        .amount(BigDecimal.valueOf(0.13281))
                        .total(BigDecimal.valueOf(0.01135439))
                        .build(),
                ResultTrade.builder()
                        .rate(BigDecimal.valueOf(0.07000304))
                        .amount(BigDecimal.valueOf(0.15009323))
                        .total(BigDecimal.valueOf(0.01050698))
                        .build());

        assertEquals(BigDecimal.valueOf(0.08145276), TradeCalculator.getResultRate(resultTrades, BigDecimal.ZERO));
    }

    @Test
    public void noTradesAmount() {
        assertEquals(BigDecimal.ZERO, TradeCalculator.getResultAmount(Collections.emptyList(), BigDecimal.ZERO));
        assertEquals(BigDecimal.ONE, TradeCalculator.getResultAmount(Collections.emptyList(), BigDecimal.ONE));
    }

    @Test
    public void oneTradeAmount() {
        BigDecimal amount = BigDecimal.valueOf(0.11209781);
        List<ResultTrade> resultTrades = Collections.singletonList(ResultTrade.builder()
                .amount(amount)
                .build());

        assertEquals(amount, TradeCalculator.getResultAmount(resultTrades, BigDecimal.ZERO));
    }

    @Test
    public void manyTradesAmount() {
        List<ResultTrade> resultTrades = Arrays.asList(ResultTrade.builder()
                        .amount(BigDecimal.valueOf(0.11209781))
                        .build(),
                ResultTrade.builder()
                        .amount(BigDecimal.valueOf(0.13209781))
                        .build(),
                ResultTrade.builder()
                        .amount(BigDecimal.valueOf(0.13547004))
                        .build());

        assertEquals(BigDecimal.valueOf(0.37966566), TradeCalculator.getResultAmount(resultTrades, BigDecimal.ZERO));
    }

}
