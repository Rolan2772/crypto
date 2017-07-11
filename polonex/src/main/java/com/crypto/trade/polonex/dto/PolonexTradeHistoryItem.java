package com.crypto.trade.polonex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PolonexTradeHistoryItem {

    @JsonProperty("globalTradeID")
    private Long globalTradeId;
    @JsonProperty("tradeID")
    private Long tradeId;
    private ZonedDateTime date;
    private String type;
    private String rate;
    private String amount;
    private String total;
}
