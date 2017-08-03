package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitAccumulator {

    BigDecimal ccyProfit = BigDecimal.ZERO;
    BigDecimal percentageProfit = BigDecimal.ZERO;
    BigDecimal netCcyProfit = BigDecimal.ZERO;
    BigDecimal netPercentageProfit = BigDecimal.ZERO;

    public void addCcyProfit(BigDecimal value) {
        ccyProfit = ccyProfit.add(value);
        netCcyProfit = netCcyProfit.add(TradeCalculator.applyFee(ccyProfit));
    }

    public void addPercentageProfit(BigDecimal value) {
        percentageProfit = percentageProfit.add(value);
        netPercentageProfit = netPercentageProfit.add(TradeCalculator.applyFee(percentageProfit));
    }


}
