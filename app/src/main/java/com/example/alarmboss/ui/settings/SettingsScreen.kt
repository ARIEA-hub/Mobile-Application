package com.example.alarmboss.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.alarmboss.R
import com.example.alarmboss.data.ContentStore
import com.example.alarmboss.util.SnoozeSettings
import kotlinx.coroutines.launch

/**
 * Where the user supplies their own content for Medium-mode tasks: reading passages and
 * lyric lines (we never ship copyrighted text), plus the target barcode/QR value for the
 * barcode task. One passage/line per row of the text box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val store = remember { ContentStore(context) }
    val scope = rememberCoroutineScope()

    var passagesText by remember { mutableStateOf("") }
    var lyricsText by remember { mutableStateOf("") }
    var barcodeTarget by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        store.readingPassages.collect { passagesText = it.joinToString("\n---\n") }
    }
    LaunchedEffect(Unit) {
        store.lyricLines.collect { lyricsText = it.joinToString("\n") }
    }
    LaunchedEffect(Unit) {
        store.barcodeTarget.collect { barcodeTarget = it ?: "" }
    }

    val snoozeSettings = remember { SnoozeSettings(context) }
    var snoozeMinutes by remember { mutableIntStateOf(snoozeSettings.minutes) }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.settings_snooze_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.settings_snooze_body),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SnoozeSettings.OPTIONS.forEach { option ->
                    FilterChip(
                        selected = snoozeMinutes == option,
                        onClick = {
                            snoozeMinutes = option
                            snoozeSettings.minutes = option
                        },
                        label = { Text(stringResource(R.string.settings_snooze_option, option)) }
                    )
                }
            }

            HorizontalDivider()

            Text(stringResource(R.string.settings_content_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_content_body),
                style = MaterialTheme.typography.bodySmall
            )

            Text(stringResource(R.string.settings_passages_title), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = passagesText, onValueChange = { passagesText = it },
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Button(onClick = {
                scope.launch { store.setReadingPassages(passagesText.split("---").map { it.trim() }.filter { it.isNotBlank() }) }
            }) { Text(stringResource(R.string.settings_save_passages)) }

            Text(stringResource(R.string.settings_lyrics_title), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = lyricsText, onValueChange = { lyricsText = it },
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Button(onClick = {
                scope.launch { store.setLyricLines(lyricsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }) }
            }) { Text(stringResource(R.string.settings_save_lyrics)) }

            Text(stringResource(R.string.settings_barcode_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.settings_barcode_body),
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(value = barcodeTarget, onValueChange = { barcodeTarget = it }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { scope.launch { store.setBarcodeTarget(barcodeTarget) } }) { Text(stringResource(R.string.settings_save_barcode)) }
        }
    }
}
