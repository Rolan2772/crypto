package com.crypto.trade.poloniex.services.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateTimeUtils {

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}
