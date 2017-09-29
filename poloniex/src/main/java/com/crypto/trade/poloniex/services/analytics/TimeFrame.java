package com.crypto.trade.poloniex.services.analytics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum TimeFrame {

    ONE_MINUTE("minute-1",
            1L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR),
    FIVE_MINUTES("minute-5",
            5L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR),
    FIFTEEN_MINUTES("minute-15",
            15L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR),
    THIRTY_MINUTES("minute-30",
            30L,
            ChronoUnit.MINUTES,
            ChronoField.MINUTE_OF_HOUR),
    ONE_HOUR("hour-1",
            1L,
            ChronoUnit.HOURS,
            ChronoField.HOUR_OF_DAY),
    TWO_HOURS("hour-2",
            2L,
            ChronoUnit.HOURS,
            ChronoField.HOUR_OF_DAY),
    FOUR_HOURS("hour-4",
            4L,
            ChronoUnit.HOURS,
            ChronoField.HOUR_OF_DAY);

    private String displayName;
    private Long duration;
    private transient TemporalUnit timeUnit;
    private transient ChronoField timeField;

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
