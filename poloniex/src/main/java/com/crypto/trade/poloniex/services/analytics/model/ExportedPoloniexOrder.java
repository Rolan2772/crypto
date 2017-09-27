package com.crypto.trade.poloniex.services.analytics.model;

import com.crypto.trade.poloniex.services.analytics.TradingAction;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExportedPoloniexOrder {

    private String name;
    private Long orderId;
    private int index;
    private String rate;
    private String amount;
    private TradingAction tradingAction;
}
