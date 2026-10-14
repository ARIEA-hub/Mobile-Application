package com.example.alarmboss.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun StreakCalendar(wakeDates: Set<LocalDate>, modifier: Modifier = Modifier) {
    // Generate a simple 7-day view for the past few weeks
    // For simplicity, we just show a 7x4 grid of recent days
    val today = LocalDate.now()
    val daysToShow = 28
    val startDate = today.minusDays(daysToShow.toLong() - 1)

    Column(modifier = modifier) {
        Text("Past 4 Weeks", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (col in 0 until 4) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until 7) {
                        val dayOffset = (col * 7) + row
                        val date = startDate.plusDays(dayOffset.toLong())
                        val isWakeDay = date in wakeDates
                        val isToday = date == today
                        
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isWakeDay -> MaterialTheme.colorScheme.primary
                                        date.isAfter(today) -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}
