package com.crypto.trade.polonex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "time")
public class Ticker implements Comparable<Ticker> {

    private Instant time;
    private String currencyPair;
    private BigDecimal last;
    private BigDecimal lowestAsk;
    private BigDecimal highestBid;
    private BigDecimal percentChange;
    private BigDecimal baseVolume;
    private BigDecimal quoteVolume;
    private boolean isFrozen;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;

    @Override
    public int compareTo(Ticker other) {
        return this.time.compareTo(other.time);
    }
}
