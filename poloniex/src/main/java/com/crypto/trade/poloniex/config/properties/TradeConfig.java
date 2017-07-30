package com.crypto.trade.poloniex.config.properties;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeConfig {

    private boolean realPrice;
    private BigDecimal minBtcTradeAmount;
    private BigDecimal realBtcTradeAmount;
}
