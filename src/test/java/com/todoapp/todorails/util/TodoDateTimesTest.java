package com.todoapp.todorails.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TodoDateTimesTest {

    @Test
    void convertsSixPmIndiaTimeToUtcAndBackWithoutDrift() {
        LocalDateTime sixPmIst = LocalDateTime.of(2026, 11, 17, 18, 0);

        LocalDateTime storedUtc = TodoDateTimes.istToUtc(sixPmIst);

        assertEquals(LocalDateTime.of(2026, 11, 17, 12, 30), storedUtc);
        assertEquals(sixPmIst, TodoDateTimes.utcToIst(storedUtc));
    }

    @Test
    void keepsNullDueDatesNullInBothDirections() {
        assertNull(TodoDateTimes.istToUtc(null));
        assertNull(TodoDateTimes.utcToIst(null));
    }
}
