package saf.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class ConverterTest {

    @AllArgsConstructor
    @Getter
    public enum TestClient {

        JURIDICAL("client.type.caption.juridical"),
        PHYSICAL("client.type.caption.physical");

        private final String key;

    }


    @Test
    public void test() {

        Converter cr = Converter.INSTANCE_DEFAULT;

        LocalDate now = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = Converter.INSTANCE_DEFAULT.getDateFormatter();
        String formattedNow = dateTimeFormatter.format(now);




        {
            Object convert4 = cr.convert(String.class, now, null);
            assertEquals(formattedNow, convert4);
        }

        {
            String convert4 = cr.convertType(String.class, now, null);
            LocalDate date = cr.convertType(LocalDate.class, convert4);
            assertEquals(date, now);
        }
        {
            LocalTime time = LocalTime.now();
            Object convert7 = cr.convert(Long.class, time);
            Object convert8 = cr.convert(LocalTime.class, convert7);
            Object convert9 = cr.convert(Long.class, convert8);
            assertEquals(time, convert8);
            assertEquals(convert9, convert7);
        }

        {
            LocalTime time = LocalTime.now();
            Object convert7 = cr.convert(String.class, time);
            Object convert8 = cr.convert(LocalTime.class, convert7);
            Object convert9 = cr.convert(String.class, convert8);
            assertEquals(time, convert8);
            assertEquals(convert9, convert7);
        }

        cr.putAsEnum(TestClient.class);

        {
            Object convert9 = cr.convert(String.class, TestClient.JURIDICAL, null);
            assertEquals(TestClient.JURIDICAL.toString(), convert9);
        }

        {
            Long convert10 = cr.convertType(Long.class, TestClient.JURIDICAL, null);
            assertEquals(TestClient.JURIDICAL.ordinal(), convert10.intValue());
        }

        {
            TestClient convert10 = cr.convertType(TestClient.class, TestClient.JURIDICAL.toString(), null);
            assertEquals(TestClient.JURIDICAL, convert10);
        }

        {
            TestClient convert10 = cr.convertType(TestClient.class, 0L, null);
            assertEquals(TestClient.JURIDICAL, convert10);
        }

        {
            TestClient convert10 = cr.convertType(TestClient.class, true, null);
            assertEquals(TestClient.PHYSICAL, convert10);
        }

        {
            BigDecimal decimal = new BigDecimal("1.0");
            BigDecimal decimal2 = cr.convertType(BigDecimal.class, decimal, null);
            assertEquals(decimal, decimal2);
        }
    }
}