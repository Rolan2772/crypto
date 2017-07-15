package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class OrderTrade {

    @JsonProperty("tradeID")
    private Long tradeId;
    private BigDecimal amount;
    private BigDecimal fee;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+0")
    private ZonedDateTime date;
}
