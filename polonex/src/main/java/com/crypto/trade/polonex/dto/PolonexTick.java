package com.crypto.trade.polonex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class PolonexTick {

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
