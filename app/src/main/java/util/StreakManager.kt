
package com.example.alarmboss.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StreakManager(context: Context) {
    private val prefs = context.getSharedPreferences("alarm_boss_prefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getStreak(): Int = prefs.getInt("current_streak", 0)

    fun recordSuccessfulWakeUp() {
        val today = dateFormat.format(Date())
        val lastDate = prefs.getString("last_success_date", "")
        var currentStreak = getStreak()

        if (lastDate != today) {
            currentStreak += 1
            prefs.edit()
                .putInt("current_streak", currentStreak)
                .putString("last_success_date", today)
                .apply()
        }
    }

    fun resetStreak() {
        prefs.edit().putInt("current_streak", 0).apply()
    }
}