package com.example.alarmboss.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.data.ProfileRepository
import com.example.alarmboss.data.UserProfile
import com.example.alarmboss.data.WakeEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StatsViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    val profile: StateFlow<UserProfile> = profileRepository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val wakeEvents: StateFlow<List<WakeEvent>> = profileRepository.observeWakeEvents().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getWakeDates(events: List<WakeEvent>): Set<LocalDate> {
        return events.map { 
            Instant.ofEpochMilli(it.dismissedAt).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }
}
