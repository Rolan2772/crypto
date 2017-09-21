package com.crypto.trade.utils;

import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.Tick;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TestCandlesFactory {

    public static Tick createCandle() {
        Tick tick = new BaseTick(Duration.ofMinutes(1), ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        tick.addTrade(0.25, 0.1);
        return tick;
    }
}
