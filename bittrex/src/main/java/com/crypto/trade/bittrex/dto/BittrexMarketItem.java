package com.crypto.trade.bittrex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BittrexMarketItem {

    @JsonProperty("MarketCurrency")
    private String marketCurrency;
    @JsonProperty("BaseCurrency")
    private String baseCurrency;
    @JsonProperty("MinTradeSize")
    private BigDecimal minTradeSize;
    @JsonProperty("MarketName")
    private String marketName;
    @JsonProperty("IsActive")
    private boolean isActive;
    @JsonProperty("Created")
    private LocalDateTime created;
}
