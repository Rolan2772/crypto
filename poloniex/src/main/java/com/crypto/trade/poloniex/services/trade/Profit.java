package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Profit {

    BigDecimal buySpent = BigDecimal.ZERO;
    BigDecimal netSellGain = BigDecimal.ZERO;
    BigDecimal grossSellGain = BigDecimal.ZERO;
    BigDecimal volume = BigDecimal.ZERO;

    // @TODO: move to lamda's
    public void accumulate(BigDecimal buySpent, BigDecimal netSellGain, BigDecimal grossSellGain) {
        this.buySpent = this.buySpent.add(buySpent);
        this.netSellGain = this.netSellGain.add(netSellGain);
        this.grossSellGain = this.grossSellGain.add(grossSellGain);
    }

    public void accumulateVolume(BigDecimal volume) {
        this.volume = this.volume.add(volume);
    }

    public BigDecimal getNetProfit() {
        return netSellGain.subtract(buySpent);
    }

    public BigDecimal getGrossProfit() {
        return grossSellGain.subtract(buySpent);
    }

    // @TODO: move to utils
    public BigDecimal getNetPercent() {
        BigDecimal netProfit = getNetProfit();
        boolean hasNetProfit = netProfit.compareTo(BigDecimal.ZERO) != 0;
        return hasNetProfit
                ? CalculationsUtils.divide(netProfit, volume)
                : BigDecimal.ZERO;
    }

    public BigDecimal getGrossPercent() {
        BigDecimal grossProfit = getGrossProfit();
        boolean hasGrossProfit = grossProfit.compareTo(BigDecimal.ZERO) != 0;
        return hasGrossProfit
                ? CalculationsUtils.divide(grossProfit, volume)
                : grossProfit;
    }
}
