package saf.utils;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Конвертер для примитивных типов данных
 * Используется {@link Converter}
 */
public class ConverterPrimitive {


    private static final Logger LOG = Logger.getLogger(ConverterPrimitive.class.getName());

    private DateTimeFormatter dateTimeFormatter;
    private DateTimeFormatter dateFormatter;
    private ZoneId zoneId;

    ConverterPrimitive(DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateFormatter, ZoneId zoneId) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.dateFormatter = dateFormatter;
        this.zoneId = zoneId;
    }


    public Double toDouble(Enum e) {
        return (double) e.ordinal();
    }

    public Double toDouble(LocalTime e) {
        return (double) getAsLong(e);
    }

    private long getAsLong(LocalTime e) {
        return e.toNanoOfDay();
    }

    public Boolean toBoolean(Enum e) {
        return e.ordinal() > 0;
    }

    public Boolean toBoolean(LocalTime e) {
        return getAsLong(e) > 0;
    }

    public Integer toInteger(Enum e) {
        return e.ordinal();
    }

    public BigDecimal toDecimal(LocalTime e) {
        return new BigDecimal(getAsLong(e));
    }

    public BigDecimal toDecimal(LocalDateTime e) {
        return new BigDecimal(e.atZone(zoneId).toEpochSecond());
    }

    public BigDecimal toDecimal(Enum e) {
        return new BigDecimal(e.ordinal());
    }

    public Long toLong(LocalTime e) {
        return getAsLong(e);
    }

    public Object self(Object o) {
        return selfStatic(o);
    }

    public static Object selfStatic(Object o) {
        return o;
    }

    public Object empty(Object o) {
        LOG.warning("Converter function is empty [" + o + "]");
        return null;
    }

    public Number toNumber(Boolean bool) {
        return bool ? 1 : 0;
    }

    public Long toLong(Boolean bool) {
        return bool ? 1L : 0L;
    }

    public Double toDouble(Boolean bool) {
        return bool ? 1D : 0D;
    }

    public Boolean fromNumber(Number num) {
        return num.intValue() > 0;
    }

    public LocalTime fromNum(Number n) {
        return LocalTime.ofNanoOfDay(n.longValue());
    }

    public LocalDateTime ldtFromNum(Number n) {
        return Instant.ofEpochMilli(n.longValue()).atZone(zoneId).toLocalDateTime();
    }

    public Boolean toBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    public Long toLong(Enum e) {
        return (long) e.ordinal();
    }

    public String formatLD(LocalDate t) {
        return dateFormatter.format(t);
    }

    public String formatLDT(LocalDateTime t) {
        return dateTimeFormatter.format(t);
    }

    public LocalDate toLocalDate(String value) {
        return LocalDate.parse(value, dateFormatter);
    }

    public Long toLong(LocalDate localDate) {
        return localDate.toEpochDay();
    }

    public LocalDate toLocalDate(Long longValue) {
        return Instant.ofEpochMilli(longValue).atZone(zoneId).toLocalDate();
    }

    public LocalDateTime toLocalDateTime(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }
}
