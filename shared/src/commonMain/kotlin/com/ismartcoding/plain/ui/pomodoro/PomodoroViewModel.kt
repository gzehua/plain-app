package com.ismartcoding.plain.ui.pomodoro


import com.ismartcoding.plain.ui.state.launchSafe

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.pomodoro.PomodoroClient
import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.models.PomodoroState
import com.ismartcoding.plain.models.PomodoroToday
import kotlinx.coroutines.Job

class PomodoroViewModel : ViewModel() {

    var currentState = mutableStateOf(PomodoroState.WORK)
    var isRunning = mutableStateOf(false)
    var adjustJob = mutableStateOf<Job?>(null)
    var isPaused = mutableStateOf(false)
    var timeLeft = mutableIntStateOf(UserPrefs.pomodoroSettings.value.workDurationMin * 60)
    var completedCount = mutableIntStateOf(0)
    var currentRound = mutableIntStateOf(1)
    var settings = mutableStateOf(UserPrefs.pomodoroSettings.value)

    val showSettings = mutableStateOf(false)

    private var totalSeconds = UserPrefs.pomodoroSettings.value.workDurationMin * 60

    suspend fun loadAsync() {
        settings.value = UserPrefs.pomodoroSettings.value
        refreshAsync()
    }
    suspend fun refreshAsync() { applySnapshot(PomodoroClient.today()) }
    fun applySnapshot(today: PomodoroToday) {
        currentState.value = today.state
        completedCount.intValue = today.completedCount
        currentRound.intValue = today.currentRound
        timeLeft.intValue = today.timeLeftSec
        totalSeconds = today.totalTimeSec
        isRunning.value = today.isRunning
        isPaused.value = today.isPaused
    }
    fun startSession() { viewModelScope.launchSafe { applySnapshot(PomodoroClient.start(timeLeft.intValue)) } }
    fun pauseSession() { viewModelScope.launchSafe { applySnapshot(PomodoroClient.pause()) } }
    fun resetTimer() { viewModelScope.launchSafe { applySnapshot(PomodoroClient.stop()) } }
    fun adjustTime(seconds: Int) { viewModelScope.launchSafe { applySnapshot(PomodoroClient.adjust(seconds)) } }
    fun updateTimeForCurrentState() { viewModelScope.launchSafe { loadAsync() } }
    fun formatTime(seconds: Int): String = "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
    fun getTotalSeconds(): Int = totalSeconds
}
