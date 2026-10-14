package com.example.alarmboss.ui.ringing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.AlarmBossApp
import com.example.alarmboss.data.Alarm
import com.example.alarmboss.data.UserProfile
import com.example.alarmboss.util.StreakManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmRingingViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AlarmBossApp
    private val repository = app.repository
    private val profileRepository = app.profileRepository

    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm

    // Snapshot taken before this wake-up is recorded, so boss day and difficulty stay stable
    // for the whole ring instead of shifting once the profile updates on dismiss.
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    val ringStartedAt: Long = System.currentTimeMillis()
    var firstTouchAt: Long = 0L
        private set

    /** Streak including today, fixed when the ring starts so a midnight dismiss can't change the plan. */
    var projectedStreak: Int = 0
        private set

    fun load(id: Long) {
        viewModelScope.launch {
            // Profile and streak first: the screen renders once the alarm is non-null.
            _profile.value = profileRepository.getProfile()
            projectedStreak = StreakManager(app).projectedStreak()
            _alarm.value = repository.getAlarm(id)
        }
    }

    fun onFirstTouch() {
        if (firstTouchAt == 0L) firstTouchAt = System.currentTimeMillis()
    }
}
