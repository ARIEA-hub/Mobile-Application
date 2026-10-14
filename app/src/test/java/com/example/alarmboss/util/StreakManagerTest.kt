package com.example.alarmboss.util

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakManagerTest {

    private val today = LocalDate.of(2026, 9, 22)

    @Test
    fun `first ever wake-up starts streak at 1`() {
        assertEquals(1, StreakManager.nextStreak(0, null, today))
    }

    @Test
    fun `waking up the day after the last success extends the streak`() {
        assertEquals(6, StreakManager.nextStreak(5, today.minusDays(1), today))
    }

    @Test
    fun `waking up again the same day does not double-count`() {
        assertEquals(5, StreakManager.nextStreak(5, today, today))
    }

    @Test
    fun `a gap of a missed day restarts the streak at 1`() {
        assertEquals(1, StreakManager.nextStreak(9, today.minusDays(2), today))
    }
}
