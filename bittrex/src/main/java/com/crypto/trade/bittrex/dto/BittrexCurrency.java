package com.crypto.trade.bittrex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BittrexCurrency {

    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("coinType")
    private String coinType;
}
