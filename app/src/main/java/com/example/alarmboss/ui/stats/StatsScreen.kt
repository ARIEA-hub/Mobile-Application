package com.example.alarmboss.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.alarmboss.R

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val profile by viewModel.profile.collectAsState()
    val wakeEvents by viewModel.wakeEvents.collectAsState()
    val scrollState = rememberScrollState()

    val currentLevel = profile.level
    val totalXp = profile.totalXp
    
    // XP needed for next level: inverse of floor(sqrt(X/50)) + 1
    // (level)^2 * 50
    val currentLevelXp = (currentLevel - 1) * (currentLevel - 1) * 50
    val nextLevelXp = currentLevel * currentLevel * 50
    val progress = (totalXp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp).toFloat().coerceAtLeast(1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.displayMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.stats_level, currentLevel), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.stats_xp_progress, totalXp, nextLevelXp), style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.stats_current_streak), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.stats_streak_value, profile.currentStreak), style = MaterialTheme.typography.headlineMedium)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.stats_bosses_defeated), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.stats_bosses_value, profile.bossesDefeated), style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.stats_records), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(pluralStringResource(R.plurals.stats_longest_streak, profile.longestStreak, profile.longestStreak))
                Text(stringResource(R.string.stats_total_wake_ups, profile.totalWakeUps))
                val avgReaction = if (profile.averageReactionMs > 0) {
                    stringResource(R.string.stats_seconds, profile.averageReactionMs / 1000)
                } else {
                    stringResource(R.string.stats_not_available)
                }
                Text(stringResource(R.string.stats_avg_reaction, avgReaction))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                StreakCalendar(wakeDates = viewModel.getWakeDates(wakeEvents))
            }
        }
    }
}
