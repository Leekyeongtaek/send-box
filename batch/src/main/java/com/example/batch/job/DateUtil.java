package com.example.batch.job;

import java.time.LocalDateTime;

public class DateUtil {

    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr);
    }
}
