package com.crypto.trade.bittrex.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BittrexMarketsResponse {

    private boolean success;
    private String message;
    private List<BittrexMarketItem> result = new ArrayList<>();
}
