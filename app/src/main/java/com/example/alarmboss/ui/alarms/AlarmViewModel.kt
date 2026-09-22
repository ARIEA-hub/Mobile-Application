package com.example.alarmboss.ui.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.AlarmBossApp
import com.example.alarmboss.alarm.AlarmScheduler
import com.example.alarmboss.data.Alarm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.alarmboss.util.StreakManager
class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val streakManager = StreakManager(application)

    private val repository = (application as AlarmBossApp).repository
    private val scheduler = AlarmScheduler(application)

    private val _streak = MutableStateFlow(streakManager.getStreak())
    val streak: StateFlow<Int> = _streak.asStateFlow()
    val alarms: StateFlow<List<Alarm>> = repository.observeAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun loadStreak() {
        _streak.value = streakManager.getStreak()
    }
    fun save(alarm: Alarm) = viewModelScope.launch {
        val id = repository.save(alarm)
        val saved = alarm.copy(id = if (alarm.id == 0L) id else alarm.id)
        if (saved.isEnabled) scheduler.schedule(saved) else scheduler.cancel(saved.id)
    }

    fun delete(alarm: Alarm) = viewModelScope.launch {
        scheduler.cancel(alarm.id)
        repository.delete(alarm)
    }

    fun setEnabled(alarm: Alarm, enabled: Boolean) = viewModelScope.launch {
        repository.setEnabled(alarm.id, enabled)
        if (enabled) scheduler.schedule(alarm.copy(isEnabled = true)) else scheduler.cancel(alarm.id)
    }
}
