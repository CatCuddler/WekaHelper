package com.romanuhlig.weka.time;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeHelper {

    static final DateTimeFormatter dateWithSecondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss");

    public static String getDateWithSeconds() {

        LocalDateTime currentTime = ZonedDateTime.now().toLocalDateTime();
        String currentTimeFormatted = currentTime.format(dateWithSecondsFormatter);

        return (currentTimeFormatted);

    }


}
