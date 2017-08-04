package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PoloniexOrderResponse {

    @JsonProperty("orderNumber")
    private Long orderId;
    private List<ResultTrade> resultingTrades = new ArrayList<>();
}
