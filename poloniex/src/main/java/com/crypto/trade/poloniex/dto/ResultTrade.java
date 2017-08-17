package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.xml.ws.BindingType;
import java.math.BigDecimal;

@Data
@Builder
public class ResultTrade {

    @JsonProperty("tradeID")
    private Long tradeId;
    // ETH amount
    private BigDecimal amount;
    private BigDecimal rate;
    // BTC amount
    private BigDecimal total;
}
