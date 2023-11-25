package com.example.tsgpaymentsystem.utils;

import com.example.tsgpaymentsystem.dto.DateRange;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
public class DateUtils {
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Novosibirsk"); // Moscow Time
    public static final String RANGE_DATE_FORMAT = "dd/MM/yyyy";

    public static String ddmmyyyy_hhmmssZ(ZonedDateTime dt) {
        if (dt == null)
            return null;

        ZonedDateTime viewZone = dt.withZoneSameInstant(DEFAULT_ZONE);
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(viewZone);
    }

    public static String ddmmyyyy(ZonedDateTime dt) {
        if (dt == null)
            return null;

        ZonedDateTime viewZone = dt.withZoneSameInstant(DEFAULT_ZONE); //
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(viewZone);
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(DEFAULT_ZONE);
    }

    public static ZonedDateTime todayStart() {
        return now().truncatedTo(ChronoUnit.DAYS);
    }

    public static ZonedDateTime todayEnd() {
        return todayStart().plusDays(1).minusNanos(1_000_000); // - 1 millisecond
    }

    public static ZonedDateTime parseExternalPaymentTimestamp(String date) {
        try {
            return ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date));
        } catch (Throwable ee) {
            ee.printStackTrace();
            log.error("Cannot parse date {}", date);
        }
        return null;
    }

    public static ZonedDateTime parseZ(String date) {
        try {
            LocalDate parse = LocalDate.parse(date, DateTimeFormatter.ofPattern(RANGE_DATE_FORMAT));
            return parse.atStartOfDay(DEFAULT_ZONE);
        } catch (Throwable any) {
            any.printStackTrace();
            log.error("Cannot parse date {}", date);
            return null;
        }
    }

    public static DateRange parseRange(String[] range) {
        if (range == null || range.length == 0)
            return null;

        DateRange dateRange = new DateRange();

        //"2023-11-11T14:00:00.000Z"
        dateRange.setFrom(DateUtils.parseZ(range[0]));

        if (range.length > 1)
            dateRange.setTo(DateUtils.parseZ(range[1]));

        return dateRange;
    }
}
