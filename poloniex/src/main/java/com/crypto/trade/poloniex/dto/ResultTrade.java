package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultTrade {

    @JsonProperty("tradeID")
    private Long tradeId;
    // ETH amount
    private BigDecimal amount;
    private BigDecimal rate;
    // BTC amount
    private BigDecimal total;
}
