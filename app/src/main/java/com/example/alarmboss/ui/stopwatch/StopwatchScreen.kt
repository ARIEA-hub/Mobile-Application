package com.example.alarmboss.ui.stopwatch

import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.R
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen() {
    // Activity-scoped, not nav-entry-scoped: switching tabs pops this destination.
    val activity = LocalActivity.current as ComponentActivity
    val viewModel: StopwatchViewModel = viewModel(viewModelStoreOwner = activity)
    val state = viewModel.state

    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(state.running) {
        while (state.running) {
            now = SystemClock.elapsedRealtime()
            delay(31) // ~30fps refresh, plenty for a stopwatch display
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_stopwatch)) }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(formatMillis(state.elapsed(now)), style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = viewModel::toggle) {
                    Text(stringResource(if (state.running) R.string.stopwatch_pause else R.string.stopwatch_start))
                }
                OutlinedButton(onClick = viewModel::lap) { Text(stringResource(R.string.stopwatch_lap)) }
                OutlinedButton(onClick = viewModel::reset) { Text(stringResource(R.string.stopwatch_reset)) }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.fillMaxWidth()) {
                items(viewModel.laps.size) { index ->
                    val lapNumber = viewModel.laps.size - index
                    Text(
                        stringResource(R.string.stopwatch_lap_row, lapNumber, formatMillis(viewModel.laps[index])),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val minutes = (ms / 60000)
    val seconds = (ms / 1000) % 60
    val centis = (ms % 1000) / 10
    return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, centis)
}
