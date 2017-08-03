package com.crypto.trade.poloniex.services.export;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitAccumulator {

    BigDecimal ccyProfit = BigDecimal.ZERO;
    BigDecimal percentageProfit = BigDecimal.ZERO;

    public void addCcyProfit(BigDecimal value) {
        ccyProfit = ccyProfit.add(value);
    }

    public void addPercentageProfit(BigDecimal value) {
        percentageProfit = percentageProfit.add(value);
    }

}
