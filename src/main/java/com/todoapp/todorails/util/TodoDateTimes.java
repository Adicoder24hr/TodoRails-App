package com.todoapp.todorails.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Converts the app's IST form values to and from UTC database values. */
public final class TodoDateTimes {

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private TodoDateTimes() {
    }

    public static LocalDateTime istToUtc(LocalDateTime istDateTime) {
        if (istDateTime == null) {
            return null;
        }
        return istDateTime.atZone(INDIA_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    public static LocalDateTime utcToIst(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(INDIA_ZONE)
                .toLocalDateTime();
    }
}
