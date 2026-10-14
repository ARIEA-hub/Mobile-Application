package com.example.alarmboss.ui.ringing

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alarmboss.ui.ringing.tasks.*

@Composable
fun BossFightScreen(difficulty: Int, onSolved: () -> Unit) {
    var stage by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2B0000)), // Dark red tint
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "BOSS FIGHT", 
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Stage ${stage + 1} of 3",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
        
        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = stage, label = "BossStage") { currentStage ->
                when (currentStage) {
                    0 -> MathTaskScreen(difficulty = difficulty) { stage++ }
                    1 -> SequenceTaskScreen(difficulty = difficulty) { stage++ }
                    2 -> MazeTaskScreen(difficulty = difficulty) { onSolved() }
                }
            }
        }
    }
}

