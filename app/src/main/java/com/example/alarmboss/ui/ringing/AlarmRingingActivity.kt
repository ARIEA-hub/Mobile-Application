package com.example.alarmboss.ui.ringing

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.alarmboss.AlarmBossApp
import com.example.alarmboss.alarm.AlarmReceiver
import com.example.alarmboss.alarm.AlarmScheduler
import com.example.alarmboss.alarm.AlarmService
import com.example.alarmboss.data.Alarm
import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.MediumTaskType
import com.example.alarmboss.data.MentalExerciseType
import com.example.alarmboss.data.StrictExerciseCategory
import com.example.alarmboss.data.UserProfile
import com.example.alarmboss.data.WakeEvent
import com.example.alarmboss.engine.BossFightManager
import com.example.alarmboss.engine.DifficultyEngine
import com.example.alarmboss.engine.ProgressionEngine
import com.example.alarmboss.ui.ringing.tasks.BarcodeTaskScreen
import com.example.alarmboss.ui.ringing.tasks.LyricsTaskScreen
import com.example.alarmboss.ui.ringing.tasks.MathTaskScreen
import com.example.alarmboss.ui.ringing.tasks.MazeTaskScreen
import com.example.alarmboss.ui.ringing.tasks.MemoryTaskScreen
import com.example.alarmboss.ui.ringing.tasks.PatternLockTaskScreen
import com.example.alarmboss.ui.ringing.tasks.ReadingTaskScreen
import com.example.alarmboss.ui.ringing.tasks.WordScrambleTaskScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.alarmboss.R
import com.example.alarmboss.ui.theme.AlarmBossTheme
import com.example.alarmboss.util.SnoozeSettings
import com.example.alarmboss.util.StreakManager
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Full-screen activity shown over the lock screen when an alarm fires. This is the ONLY
 * place the alarm can be dismissed from -- back/home are suppressed where the platform
 * allows it, and the underlying AlarmService keeps sound/vibration going until dismiss()
 * is actually reached, so the mode's task genuinely has to be completed.
 */
class AlarmRingingActivity : ComponentActivity() {

    private val viewModel: AlarmRingingViewModel by viewModels()
    private var dismissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        // Block the back gesture/button so the alarm can't be dismissed without completing its task.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* no-op by design */ }
        })

        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        viewModel.load(alarmId)

        setContent {
            AlarmBossTheme {
                val alarm by viewModel.alarm.collectAsState()
                val profile by viewModel.profile.collectAsState()
                Surface(
                    Modifier
                        .fillMaxSize()
                        // Observe only (Initial pass, nothing consumed): the first touch marks reaction time.
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                awaitPointerEvent(PointerEventPass.Initial)
                                viewModel.onFirstTouch()
                            }
                        }
                ) {
                    val currentAlarm = alarm
                    val currentProfile = profile
                    if (currentAlarm != null && currentProfile != null) {
                        RingingContent(
                            alarm = currentAlarm,
                            plan = planFor(currentAlarm, currentProfile, viewModel.projectedStreak),
                            onDismiss = ::dismissAlarm,
                            onSnooze = ::snoozeAlarm
                        )
                    } else {
                        LoadingContent()
                    }
                }
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun dismissAlarm() {
        // A double tap on the final step must not award XP twice.
        if (dismissed) return
        dismissed = true

        // Reaching here means the alarm's task (if any) was actually completed, not just
        // snoozed away -- that's what counts as a genuine wake-up for the streak and XP.
        val newStreak = StreakManager(this).recordSuccessfulWakeUp()
        recordProgression(newStreak)
        AlarmService.stop(this)
        finish()
    }

    private fun recordProgression(newStreak: Int) {
        val alarm = viewModel.alarm.value ?: return
        val profile = viewModel.profile.value ?: return
        val plan = planFor(alarm, profile, viewModel.projectedStreak)

        val now = System.currentTimeMillis()
        val completionMs = now - viewModel.ringStartedAt
        val reactionMs = (viewModel.firstTouchAt.takeIf { it > 0L } ?: now) - viewModel.ringStartedAt

        val outcome = ProgressionEngine.applyWakeUp(
            profile = profile,
            mode = alarm.mode,
            strictCategory = alarm.strictCategory.takeIf { alarm.mode == AlarmMode.STRICT },
            isBossFight = plan.isBossFight,
            newStreak = newStreak,
            reactionMs = reactionMs,
            completionMs = completionMs,
            today = LocalDate.now()
        )
        val event = WakeEvent(
            alarmId = alarm.id,
            triggeredAt = viewModel.ringStartedAt,
            dismissedAt = now,
            challengeType = challengeType(alarm, plan),
            difficulty = plan.difficulty,
            xpEarned = outcome.xpEarned,
            reactionTimeMs = reactionMs,
            completionTimeMs = completionMs,
            isBossFight = plan.isBossFight
        )

        // App scope, not lifecycleScope: finish() cancels the latter before the write lands.
        val app = application as AlarmBossApp
        app.appScope.launch { app.profileRepository.recordWakeUp(event, outcome.profile) }
    }

    /** Only offered in Easy mode: stops the current ring and re-fires the alarm after the chosen snooze length. */
    private fun snoozeAlarm() {
        viewModel.alarm.value?.let { alarm ->
            AlarmScheduler(this).scheduleOneOffInMinutes(alarm, SnoozeSettings(this).minutes)
        }
        AlarmService.stop(this)
        finish()
    }
}

/** What this particular ring asks of the user, decided once from the profile snapshot. */
private data class RingPlan(val isBossFight: Boolean, val difficulty: Int)

// Easy alarms stay one tap by contract; boss fights replace the task on Medium and Strict ones.
private fun planFor(alarm: Alarm, profile: UserProfile, projectedStreak: Int) = RingPlan(
    isBossFight = alarm.mode != AlarmMode.EASY && BossFightManager.isBossFightDay(projectedStreak),
    difficulty = DifficultyEngine.calculate(profile.level, projectedStreak)
)

private fun challengeType(alarm: Alarm, plan: RingPlan): String = when {
    plan.isBossFight -> "BOSS_FIGHT"
    alarm.mode == AlarmMode.EASY -> "EASY"
    alarm.mode == AlarmMode.MEDIUM -> "MEDIUM"
    alarm.strictCategory == StrictExerciseCategory.PHYSICAL -> "STRICT_PHYSICAL"
    else -> "STRICT_MENTAL"
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun RingingContent(alarm: Alarm, plan: RingPlan, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    if (plan.isBossFight) {
        BossFightScreen(difficulty = plan.difficulty, onSolved = onDismiss)
        return
    }

    val context = LocalContext.current
    val streakManager = remember { StreakManager(context) }
    val streak = remember { streakManager.getCurrentStreak() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = pluralStringResource(R.plurals.ringing_streak, streak, streak),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (alarm.mode) {
            AlarmMode.EASY -> EasyDismissScreen(
                alarm = alarm,
                snoozeMinutes = remember { SnoozeSettings(context).minutes },
                onDismiss = onDismiss,
                onSnooze = onSnooze
            )
            AlarmMode.MEDIUM -> {
                val task = remember(alarm.id) { alarm.enabledMediumTasks.randomOrNull() ?: MediumTaskType.MATH }
                when (task) {
                    MediumTaskType.MATH -> MathTaskScreen(difficulty = plan.difficulty, onSolved = onDismiss)
                    MediumTaskType.READING -> ReadingTaskScreen(onDone = onDismiss)
                    MediumTaskType.LYRICS -> LyricsTaskScreen(onDone = onDismiss)
                    MediumTaskType.BARCODE -> BarcodeTaskScreen(onScanned = onDismiss)
                    MediumTaskType.PATTERN_LOCK -> PatternLockTaskScreen(difficulty = plan.difficulty, onSolved = onDismiss)
                    MediumTaskType.WORD_SCRAMBLE -> WordScrambleTaskScreen(difficulty = plan.difficulty, onSolved = onDismiss)
                }
            }
            AlarmMode.STRICT -> {
                if (alarm.strictCategory == StrictExerciseCategory.MENTAL) {
                    when (alarm.mentalExerciseType) {
                        MentalExerciseType.MEMORY_GRID -> MemoryTaskScreen(onSolved = onDismiss)
                        MentalExerciseType.MAZE -> MazeTaskScreen(difficulty = plan.difficulty, onSolved = onDismiss)
                    }
                } else {
                    StrictExerciseScreen(
                        initialExercise = alarm.exerciseType,
                        durationSeconds = alarm.exerciseDurationSeconds,
                        onComplete = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun EasyDismissScreen(alarm: Alarm, snoozeMinutes: Int, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            alarm.label.ifBlank { stringResource(R.string.ringing_default_label) },
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDismiss) { Text(stringResource(R.string.ringing_dismiss)) }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSnooze) { Text(stringResource(R.string.ringing_snooze, snoozeMinutes)) }
    }
}
