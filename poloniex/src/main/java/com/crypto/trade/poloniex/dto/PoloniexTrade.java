package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PoloniexTrade {

    @JsonProperty("tradeId")
    private Long tradeId;
    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Kiev")
    private ZonedDateTime tradeTime;
    private String amount;
    private String rate;
    private String total;
    private String type;

    public PoloniexTrade(PoloniexHistoryTrade historyTrade) {
        this.tradeId = historyTrade.getTradeId();
        this.tradeTime = historyTrade.getDate();
        this.amount = historyTrade.getAmount();
        this.rate = historyTrade.getRate();
        this.total = historyTrade.getTotal();
        this.type = historyTrade.getType();
    }
}
