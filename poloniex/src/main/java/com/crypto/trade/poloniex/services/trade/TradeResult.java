package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeResult {

    BigDecimal buySpent = BigDecimal.ZERO;
    BigDecimal netSellGain = BigDecimal.ZERO;
    BigDecimal grossSellGain = BigDecimal.ZERO;
    BigDecimal volume = BigDecimal.ZERO;
    Integer tradesCount = 0;

    // @TODO: move to lamda's
    public void accumulate(BigDecimal buySpent, BigDecimal netSellGain, BigDecimal grossSellGain) {
        this.buySpent = this.buySpent.add(buySpent);
        this.netSellGain = this.netSellGain.add(netSellGain);
        this.grossSellGain = this.grossSellGain.add(grossSellGain);
    }

    public void accumulateVolume(BigDecimal volume) {
        this.volume = this.volume.add(volume);
    }

    public void accumulateTradesCount(Integer tradesCount) {
        this.tradesCount += tradesCount;
    }



}
