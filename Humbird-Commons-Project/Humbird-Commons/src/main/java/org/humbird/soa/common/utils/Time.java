package org.humbird.soa.common.utils;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A helper class for working with times in various units
 * 
 * @version 
 */
public class Time {
    private long number;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public Time(long number, TimeUnit timeUnit) {
        this.number = number;
        this.timeUnit = timeUnit;
    }

    public static Time millis(long value) {
        return new Time(value, TimeUnit.MILLISECONDS);
    }

    public static Time seconds(long value) {
        return new Time(value, TimeUnit.SECONDS);
    }

    public static Time minutes(long value) {
        return new Time(minutesAsSeconds(value), TimeUnit.MILLISECONDS);
    }

    public static Time hours(long value) {
        return new Time(hoursAsSeconds(value), TimeUnit.MILLISECONDS);
    }

    public static Time days(long value) {
        return new Time(daysAsSeconds(value), TimeUnit.MILLISECONDS);
    }

    public long toMillis() {
        return timeUnit.toMillis(number);
    }

    public Date toDate() {
        return new Date(toMillis());
    }

    public long getNumber() {
        return number;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    protected static long minutesAsSeconds(long value) {
        return value * 60;
    }

    protected static long hoursAsSeconds(long value) {
        return minutesAsSeconds(value) * 60;
    }

    protected static long daysAsSeconds(long value) {
        return hoursAsSeconds(value) * 24;
    }

    @Override
    public String toString() {
        return number + " " + timeUnit.toString().toLowerCase(Locale.ENGLISH);
    }
}
