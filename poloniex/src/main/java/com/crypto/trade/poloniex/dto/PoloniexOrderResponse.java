package com.crypto.trade.poloniex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoloniexOrderResponse {

    @JsonProperty("orderNumber")
    private Long orderId;
    //@TODO: order response contains real trades
}
