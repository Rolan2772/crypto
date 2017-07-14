package com.crypto.trade.poloniex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PoloniexTick {

    private Long tradeId;
    private ZonedDateTime time;
    private String currencyPair;
    private String last;
    private String lowestAsk;
    private String highestBid;
    private String percentChange;
    private String baseVolume;
    private String quoteVolume;
    private boolean isFrozen;
    private String dayHigh;
    private String dayLow;

}
