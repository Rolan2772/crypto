package com.crypto.trade.poloniex.services.analytics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum TimeFrame {

    ONE_MINUTE("1minute",
            1L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR),
    FIVE_MINUTES("5minute",
            5L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR),
    FIFTEEN_MINUTES("15minute",
            15L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR);

    private String displayName;
    private Long duration;
    private TemporalUnit timeUnit;
    private ChronoField timeField;

    TimeFrame(String displayName, Long duration, TemporalUnit timeUnit, ChronoField timeField) {
        this.displayName = displayName;
        this.duration = duration;
        this.timeUnit = timeUnit;
        this.timeField = timeField;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getDuration() {
        return duration;
    }

    public TemporalUnit getTimeUnit() {
        return timeUnit;
    }

    public ChronoField getTimeField() {
        return timeField;
    }

    public Duration getFrameDuration() {
        return Duration.of(duration, timeUnit);
    }

    public ZonedDateTime calculateEndTime(ZonedDateTime tickTime) {
        ZonedDateTime truncatedTime = tickTime.truncatedTo(timeUnit);
        Long diff = duration - truncatedTime.get(timeField) % duration;
        return truncatedTime.plus(diff, timeUnit);
    }
}
