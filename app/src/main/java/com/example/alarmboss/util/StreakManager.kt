package com.example.alarmboss.util

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class StreakManager(context: Context) {
    private val prefs = context.getSharedPreferences("alarm_boss_prefs", Context.MODE_PRIVATE)

    fun getCurrentStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    /** Call once per alarm the user actually wakes up to and dismisses. */
    fun recordSuccessfulWakeUp(today: LocalDate = LocalDate.now()) {
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        prefs.edit()
            .putInt(KEY_STREAK, nextStreak(getCurrentStreak(), lastDate, today))
            .putString(KEY_LAST_DATE, today.toString())
            .apply()
    }

    companion object {
        private const val KEY_STREAK = "current_streak"
        private const val KEY_LAST_DATE = "last_success_date"

        /**
         * Pure streak transition, kept separate from SharedPreferences so it's unit testable:
         * same day is a no-op, the next day extends the streak, any gap (or a first wake-up)
         * restarts it at 1.
         */
        fun nextStreak(currentStreak: Int, lastDate: LocalDate?, today: LocalDate): Int {
            if (lastDate == null) return 1
            val daysBetween = ChronoUnit.DAYS.between(lastDate, today)
            return when {
                daysBetween <= 0L -> currentStreak
                daysBetween == 1L -> currentStreak + 1
                else -> 1
            }
        }
    }
}
