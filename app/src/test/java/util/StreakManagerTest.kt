package com.example.alarmboss.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakManagerTest {

    private val today = "2026-09-22"
    private val yesterday = "2026-09-21"

    @Test
    fun `first ever wake-up starts streak at 1`() {
        assertEquals(1, StreakManager.nextStreak(0, null, today, yesterday))
    }

    @Test
    fun `waking up the day after the last success extends the streak`() {
        assertEquals(6, StreakManager.nextStreak(5, yesterday, today, yesterday))
    }

    @Test
    fun `waking up again the same day does not double-count`() {
        assertEquals(5, StreakManager.nextStreak(5, today, today, yesterday))
    }

    @Test
    fun `a gap of a missed day restarts the streak at 1`() {
        assertEquals(1, StreakManager.nextStreak(9, "2026-09-10", today, yesterday))
    }
}
