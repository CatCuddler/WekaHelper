package com.romanuhlig.weka.time;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides time related helper functions
 *
 * @author Roman Uhlig
 */
public class TimeHelper {

    static final DateTimeFormatter dateWithSecondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss");

    /**
     * Get a string representing the current date and time, including seconds
     *
     * @return
     */
    public static String getDateWithSeconds() {

        LocalDateTime currentTime = ZonedDateTime.now().toLocalDateTime();
        String currentTimeFormatted = currentTime.format(dateWithSecondsFormatter);

        return (currentTimeFormatted);

    }

    /**
     * Converts the number of seconds to a string suitable for logging
     *
     * @param seconds
     * @return
     */
    public static String secondsToTimeOutput(int seconds) {
        int minutesPart = seconds / 60;
        int secondsPart = seconds % 60;

        // prepend a zero to the seconds part, if it does not have two digits already
        String secondsString = Integer.toString(secondsPart);
        if (secondsPart < 10) {
            secondsString = "0" + secondsString;
        }

        return minutesPart + ":" + secondsString;
    }

    /**
     * Converts the number of seconds to a string suitable for logging
     *
     * @param seconds
     * @return
     */
    public static String secondsToTimeOutput(long seconds) {
        return secondsToTimeOutput(Math.toIntExact(seconds));
    }
}
