package com.crypto.trade.poloniex.services.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountBalance {

    @JsonProperty("BTC")
    private String btc;
    @JsonProperty("ETH")
    private String eth;
    @JsonProperty("LTC")
    private String ltc;
    @JsonProperty("XRP")
    private String xrp;
}
