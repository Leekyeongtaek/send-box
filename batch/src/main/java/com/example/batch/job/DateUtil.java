package com.example.batch.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr);
    }
}
