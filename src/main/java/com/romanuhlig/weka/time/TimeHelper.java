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

    public static String secondsToTimeOutput(int seconds) {
        int minutesPart = seconds / 60;
        int secondsPart = seconds % 60;

        String secondsString = Integer.toString(secondsPart);
        if (secondsPart < 10) {
            secondsString = "0" + secondsString;
        }

        return minutesPart + ":" + secondsString;
    }

    public static String secondsToTimeOutput(long seconds) {
        return secondsToTimeOutput(Math.toIntExact(seconds));
    }


}
