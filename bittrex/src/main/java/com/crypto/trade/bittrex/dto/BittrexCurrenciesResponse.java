package com.crypto.trade.bittrex.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BittrexCurrenciesResponse {

    private boolean success;
    private String message;
    private List<BittrexCurrency> result = new ArrayList<>();
}
