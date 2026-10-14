package com.example.alarmboss.engine

import com.example.alarmboss.data.UserProfile

object BossFightManager {
    fun isBossFightDay(profile: UserProfile): Boolean {
        // Boss fight triggers every 7th day of a streak
        return profile.currentStreak > 0 && profile.currentStreak % 7 == 0
    }
}
