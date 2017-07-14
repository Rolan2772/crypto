package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PoloniexHistoryTrade {

    @JsonProperty("globalTradeID")
    private Long globalTradeId;
    @JsonProperty("tradeId")
    private Long tradeId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+0")
    private ZonedDateTime date;
    private String type;
    private String rate;
    private String amount;
    private String total;
}
