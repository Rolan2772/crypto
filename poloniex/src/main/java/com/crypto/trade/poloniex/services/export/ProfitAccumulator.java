package com.crypto.trade.poloniex.services.export;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitAccumulator {

    BigDecimal ccyProfit = BigDecimal.ZERO;
    BigDecimal percentageProfit = BigDecimal.ZERO;
    BigDecimal netCcyProfit = BigDecimal.ZERO;
    BigDecimal netPercentageProfit = BigDecimal.ZERO;

    public void addCcyProfit(BigDecimal profit, BigDecimal netProfit) {
        ccyProfit = ccyProfit.add(profit);
        netCcyProfit = netCcyProfit.add(netProfit);
    }

    public void addPercentageProfit(BigDecimal profit, BigDecimal netProfit) {
        percentageProfit = percentageProfit.add(profit);
        netPercentageProfit = netPercentageProfit.add(netProfit);
    }
}
