package com.crypto.trade.poloniex.services.analytics.strategies;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.model.ShortBuyAnalytics;
import com.crypto.trade.poloniex.services.analytics.model.TrendAnalytics;
import com.crypto.trade.poloniex.services.analytics.model.TripleEmaAnalytics;
import com.crypto.trade.poloniex.storage.analytics.AnalyticsStorage;
import org.springframework.beans.factory.annotation.Autowired;

import static com.crypto.trade.poloniex.storage.analytics.IndicatorType.*;

public class AnalyticsHelper {

    @Autowired
    private AnalyticsStorage analyticsStorage;

    public ShortBuyAnalytics getShortBuyAnalytics(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return ShortBuyAnalytics.of(analyticsStorage.getIndicator(currencyPair, timeFrame, CLOSED_PRICE),
                analyticsStorage.getIndicator(currencyPair, timeFrame, RSI14),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA540),
                analyticsStorage.getIndicator(currencyPair, timeFrame, STOCHK14),
                analyticsStorage.getIndicator(currencyPair, timeFrame, STOCHD3));
    }

    public TrendAnalytics getRisingAnalytics(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return TrendAnalytics.of(analyticsStorage.getIndicator(currencyPair, timeFrame, CLOSED_PRICE),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA5),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA100));
    }

    public TripleEmaAnalytics getTripleEmaAnalytics(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return TripleEmaAnalytics.of(analyticsStorage.getIndicator(currencyPair, timeFrame, CLOSED_PRICE),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA5),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA_EMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame, DMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame, EMA_EMA_EMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame, TMA90));
    }
}
