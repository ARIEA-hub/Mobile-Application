package com.example.alarmboss.ui.alarms

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.R
import com.example.alarmboss.data.*
import com.example.alarmboss.util.formatTime12h
import java.time.DayOfWeek
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long?,
    onDone: () -> Unit,
    viewModel: AlarmViewModel = viewModel()
) {
    val context = LocalContext.current
    val alarms by viewModel.alarms.collectAsState()
    val base = remember(alarms, alarmId) { alarms.find { it.id == alarmId } }

    var hour by remember(base) { mutableIntStateOf(base?.hour ?: 7) }
    var minute by remember(base) { mutableIntStateOf(base?.minute ?: 0) }
    var label by remember(base) { mutableStateOf(base?.label ?: "") }
    var mode by remember(base) { mutableStateOf(base?.mode ?: AlarmMode.EASY) }
    var days by remember(base) { mutableStateOf(base?.repeatDays ?: emptySet()) }
    var soundUri by remember(base) { mutableStateOf(base?.soundUri) }
    var vibrate by remember(base) { mutableStateOf(base?.vibrate ?: true) }
    var enabledTasks by remember(base) { mutableStateOf(base?.enabledMediumTasks ?: MediumTaskType.values().toSet()) }
    var exerciseType by remember(base) { mutableStateOf(base?.exerciseType ?: ExerciseType.SQUATS) }
    var exerciseSeconds by remember(base) { mutableFloatStateOf((base?.exerciseDurationSeconds ?: 300).toFloat()) }

    // Strict category state variables
    var strictCategory by remember(base) { mutableStateOf(base?.strictCategory ?: StrictExerciseCategory.PHYSICAL) }
    var mentalExerciseType by remember(base) { mutableStateOf(base?.mentalExerciseType ?: MentalExerciseType.MEMORY_GRID) }

    val soundPicker = rememberSoundPickerLauncher { uri -> soundUri = uri }

    var showCameraWarning by remember { mutableStateOf(false) }

    fun saveAlarm() {
        viewModel.save(
            Alarm(
                id = base?.id ?: 0L,
                hour = hour, minute = minute, label = label,
                isEnabled = base?.isEnabled ?: true,
                repeatDays = days, mode = mode, soundUri = soundUri,
                vibrate = vibrate, enabledMediumTasks = enabledTasks,
                exerciseDurationSeconds = exerciseSeconds.toInt(),
                exerciseType = exerciseType,
                strictCategory = strictCategory,
                mentalExerciseType = mentalExerciseType
            )
        )
        onDone()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Only a denied camera is worth stopping for: the microphone tasks accept typing instead.
        if (results[Manifest.permission.CAMERA] == false) showCameraWarning = true else saveAlarm()
    }

    fun onSaveClicked() {
        val missing = requiredPermissions(mode, strictCategory, enabledTasks).filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) saveAlarm() else permissionLauncher.launch(missing.toTypedArray())
    }

    if (showCameraWarning) {
        AlertDialog(
            onDismissRequest = { showCameraWarning = false },
            title = { Text(stringResource(R.string.edit_camera_warning_title)) },
            text = { Text(stringResource(R.string.edit_camera_warning_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showCameraWarning = false
                    saveAlarm()
                }) { Text(stringResource(R.string.edit_camera_warning_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showCameraWarning = false }) {
                    Text(stringResource(R.string.edit_camera_warning_keep_editing))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (base == null) R.string.edit_title_new else R.string.edit_title_edit))
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    TextButton(onClick = ::onSaveClicked) { Text(stringResource(R.string.action_save)) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedButton(onClick = {
                TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show()
            }) {
                Text(stringResource(R.string.edit_time, formatTime12h(hour, minute)))
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(stringResource(R.string.edit_label)) }
            )

            Text(stringResource(R.string.edit_repeat), style = MaterialTheme.typography.titleSmall)
            DaySelector(selected = days, onChange = { days = it })

            Text(stringResource(R.string.edit_mode), style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow {
                AlarmMode.values().forEachIndexed { index, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { mode = m },
                        shape = SegmentedButtonDefaults.itemShape(index, AlarmMode.values().size)
                    ) { Text(modeName(m)) }
                }
            }

            when (mode) {
                AlarmMode.EASY -> Text(
                    stringResource(R.string.edit_easy_description),
                    style = MaterialTheme.typography.bodySmall
                )
                AlarmMode.MEDIUM -> MediumTaskPicker(enabledTasks) { enabledTasks = it }
                AlarmMode.STRICT -> StrictConfig(
                    strictCategory = strictCategory,
                    onStrictCategoryChange = { strictCategory = it },
                    exerciseType = exerciseType,
                    onExerciseTypeChange = { exerciseType = it },
                    mentalExerciseType = mentalExerciseType,
                    onMentalExerciseTypeChange = { mentalExerciseType = it },
                    seconds = exerciseSeconds,
                    onSecondsChange = { exerciseSeconds = it }
                )
            }

            Text(stringResource(R.string.edit_sound), style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { soundPicker() }) {
                Text(stringResource(if (soundUri == null) R.string.edit_sound_default else R.string.edit_sound_custom))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vibrate, onCheckedChange = { vibrate = it })
                Text(stringResource(R.string.edit_vibrate))
            }
        }
    }
}

@Composable
private fun DaySelector(selected: Set<Int>, onChange: (Set<Int>) -> Unit) {
    // Read from the configuration, not Locale.getDefault(), so labels update if the language changes.
    val locale = LocalConfiguration.current.locales[0]
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..7).forEach { iso ->
            val label = DayOfWeek.of(iso).getDisplayName(TextStyle.NARROW, locale)
            FilterChip(
                selected = selected.contains(iso),
                onClick = {
                    onChange(if (selected.contains(iso)) selected - iso else selected + iso)
                },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun MediumTaskPicker(enabled: Set<MediumTaskType>, onChange: (Set<MediumTaskType>) -> Unit) {
    Column {
        Text(
            stringResource(R.string.edit_medium_description),
            style = MaterialTheme.typography.bodySmall
        )
        MediumTaskType.values().forEach { task ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = enabled.contains(task),
                    onCheckedChange = {
                        val next = if (it) enabled + task else enabled - task
                        if (next.isNotEmpty()) onChange(next)
                    }
                )
                Text(taskName(task))
            }
        }
    }
}

@Composable
private fun StrictConfig(
    strictCategory: StrictExerciseCategory,
    onStrictCategoryChange: (StrictExerciseCategory) -> Unit,
    exerciseType: ExerciseType,
    onExerciseTypeChange: (ExerciseType) -> Unit,
    mentalExerciseType: MentalExerciseType,
    onMentalExerciseTypeChange: (MentalExerciseType) -> Unit,
    seconds: Float,
    onSecondsChange: (Float) -> Unit
) {
    Column {
        Text(
            stringResource(R.string.edit_strict_description),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        Text(stringResource(R.string.edit_strict_category), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow {
            StrictExerciseCategory.values().forEachIndexed { index, cat ->
                SegmentedButton(
                    selected = strictCategory == cat,
                    onClick = { onStrictCategoryChange(cat) },
                    shape = SegmentedButtonDefaults.itemShape(index, StrictExerciseCategory.values().size)
                ) {
                    Text(
                        stringResource(
                            if (cat == StrictExerciseCategory.PHYSICAL) R.string.edit_strict_physical
                            else R.string.edit_strict_mental
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (strictCategory == StrictExerciseCategory.PHYSICAL) {
            Text(stringResource(R.string.edit_physical_type), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow {
                ExerciseType.values().forEachIndexed { index, e ->
                    SegmentedButton(
                        selected = exerciseType == e,
                        onClick = { onExerciseTypeChange(e) },
                        shape = SegmentedButtonDefaults.itemShape(index, ExerciseType.values().size)
                    ) { Text(exerciseName(e)) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.edit_duration, seconds.toInt() / 60, seconds.toInt() % 60))
            Slider(value = seconds, onValueChange = onSecondsChange, valueRange = 60f..900f, steps = 13)
        } else {
            Text(stringResource(R.string.edit_mental_type), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow {
                MentalExerciseType.values().forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = mentalExerciseType == type,
                        onClick = { onMentalExerciseTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, MentalExerciseType.values().size)
                    ) {
                        Text(mentalName(type))
                    }
                }
            }
        }
    }
}
