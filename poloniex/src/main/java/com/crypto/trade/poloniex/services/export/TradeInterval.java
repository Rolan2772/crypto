package com.crypto.trade.poloniex.services.export;

import eu.verdelhan.ta4j.Trade;
import lombok.Value;

@Value
public class TradeInterval {

    private int entryIndex;
    private int exitIndex;

    public TradeInterval(Trade trade) {
        this.entryIndex = trade.getEntry().getIndex();
        this.exitIndex = trade.getExit().getIndex();
    }

    public String getReportView() {
        return exitIndex + "-" + entryIndex;
    }
}
