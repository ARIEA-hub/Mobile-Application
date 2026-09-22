
package com.example.alarmboss.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StreakManager(context: Context) {
    private val prefs = context.getSharedPreferences("alarm_boss_prefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getStreak(): Int = prefs.getInt("current_streak", 0)

    /** Call once per alarm the user actually wakes up to and dismisses. */
    fun recordSuccessfulWakeUp() {
        val today = dateFormat.format(Date())
        val yesterday = dateFormat.format(Date(System.currentTimeMillis() - DAY_MILLIS))
        val lastDate = prefs.getString("last_success_date", null)
        val newStreak = nextStreak(getStreak(), lastDate, today, yesterday)

        if (newStreak != getStreak() || lastDate != today) {
            prefs.edit()
                .putInt("current_streak", newStreak)
                .putString("last_success_date", today)
                .apply()
        }
    }

    fun resetStreak() {
        prefs.edit().putInt("current_streak", 0).remove("last_success_date").apply()
    }

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

        /**
         * Pure streak-transition logic, kept separate from SharedPreferences so it's unit
         * testable: a wake-up on the same day already counted is a no-op, a wake-up the day
         * right after the last one extends the streak, and any gap (or a first-ever wake-up)
         * restarts it at 1.
         */
        fun nextStreak(currentStreak: Int, lastSuccessDate: String?, today: String, yesterday: String): Int =
            when (lastSuccessDate) {
                today -> currentStreak
                yesterday -> currentStreak + 1
                else -> 1
            }
    }
}
