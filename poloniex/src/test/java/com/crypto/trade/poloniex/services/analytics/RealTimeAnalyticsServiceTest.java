package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.model.AnalyticsData;
import com.crypto.trade.poloniex.services.analytics.model.TradeData;
import com.crypto.trade.utils.TestCandlesFactory;
import com.crypto.trade.utils.TestStrategyFactory;
import eu.verdelhan.ta4j.BaseTradingRecord;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RealTimeAnalyticsServiceTest {

    private TradeData tradeData = TradeData.of(TestCandlesFactory.createCandle(), 0, Order.OrderType.BUY, BigDecimal.ONE);
    @Spy
    private BaseTradingRecord tradingRecord;

    @Spy
    @InjectMocks
    private RealTimeAnalyticsService analyticsService;

    @Test
    public void shouldNotEnter() {
        Strategy strategy = TestStrategyFactory.createNonTradingStrategy();
        AnalyticsData analyticsData = AnalyticsData.of(strategy, tradingRecord, 0);

        assertEquals(TradingAction.NO_ACTION, analyticsService.analyzeTick(analyticsData, tradeData));
    }

    @Test
    public void alreadyEntered() {
        Strategy strategy = TestStrategyFactory.createEntryStrategy();
        tradingRecord.enter(0, Decimal.ONE, Decimal.ONE);
        AnalyticsData analyticsData = AnalyticsData.of(strategy, tradingRecord, 0);

        assertEquals(TradingAction.NO_ACTION, analyticsService.analyzeTick(analyticsData, tradeData));
    }

    @Test
    public void shouldNotExit() {
        Strategy strategy = TestStrategyFactory.createNonTradingStrategy();
        tradingRecord.enter(0, Decimal.ONE, Decimal.ONE);
        AnalyticsData analyticsData = AnalyticsData.of(strategy, tradingRecord, 0);

        assertEquals(TradingAction.NO_ACTION, analyticsService.analyzeTick(analyticsData, tradeData));
    }

    @Test
    public void alreadyExited() {
        Strategy strategy = TestStrategyFactory.createExitStrategy();
        tradingRecord.enter(0, Decimal.ONE, Decimal.ONE);
        tradingRecord.exit(1, Decimal.ONE, Decimal.ONE);
        AnalyticsData analyticsData = AnalyticsData.of(strategy, tradingRecord, 0);

        assertEquals(TradingAction.NO_ACTION, analyticsService.analyzeTick(analyticsData, tradeData));
    }

    @Test
    public void shouldEnter() {
        Strategy strategy = TestStrategyFactory.createEntryStrategy();
        AnalyticsData analyticsData = AnalyticsData.of(strategy, tradingRecord, 0);

        assertEquals(TradingAction.SHOULD_ENTER, analyticsService.analyzeTick(analyticsData, tradeData));
    }

    @Test
    public void shouldExit() {
        Strategy strategy = TestStrategyFactory.createExitStrategy();
        tradingRecord.enter(0, Decimal.ONE, Decimal.ONE);
        AnalyticsData analyticsData = AnalyticsData.of(strategy, tradingRecord, 0);

        assertEquals(TradingAction.SHOULD_EXIT, analyticsService.analyzeTick(analyticsData, tradeData));
    }
}
