package com.example.alarmboss.engine

import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.StrictExerciseCategory
import com.example.alarmboss.data.UserProfile
import java.time.LocalDate
import kotlin.math.max

object ProgressionEngine {

    data class Outcome(val profile: UserProfile, val xpEarned: Int)

    /** Pure profile update for one completed wake-up: XP, level, streak records and averages. */
    fun applyWakeUp(
        profile: UserProfile,
        mode: AlarmMode,
        strictCategory: StrictExerciseCategory?,
        isBossFight: Boolean,
        newStreak: Int,
        reactionMs: Long,
        completionMs: Long,
        today: LocalDate
    ): Outcome {
        val xp = XpEngine.calculateXp(
            mode = mode,
            isBossFight = isBossFight,
            streak = newStreak,
            completionTimeMs = completionMs,
            strictCategory = strictCategory
        )
        val totalXp = profile.totalXp + xp
        val wakeUps = profile.totalWakeUps + 1
        val averageReaction = (profile.averageReactionMs * profile.totalWakeUps + reactionMs) / wakeUps

        return Outcome(
            profile = profile.copy(
                totalXp = totalXp,
                level = XpEngine.calculateLevel(totalXp),
                currentStreak = newStreak,
                longestStreak = max(profile.longestStreak, newStreak),
                totalWakeUps = wakeUps,
                bossesDefeated = profile.bossesDefeated + if (isBossFight) 1 else 0,
                lastWakeDate = today.toString(),
                averageReactionMs = averageReaction
            ),
            xpEarned = xp
        )
    }
}
