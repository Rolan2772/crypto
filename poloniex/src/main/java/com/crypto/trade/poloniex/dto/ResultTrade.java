package com.crypto.trade.poloniex.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultTrade {

    private Long tradeId;
    // ETH amount
    private BigDecimal amount;
    private BigDecimal rate;
    // BTC amount
    private BigDecimal total;
}
