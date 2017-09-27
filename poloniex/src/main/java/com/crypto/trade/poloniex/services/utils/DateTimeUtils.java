package com.crypto.trade.poloniex.services.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateTimeUtils {

    public static final DateTimeFormatter CSV_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    public static String format(TemporalAccessor temporalAccessor) {
        return CSV_FORMATTER.format(temporalAccessor);
    }

    private DateTimeUtils() {
    }
}
