package at.uastw.wopper.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.wopper.TimerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerState(
    val minutes: Int = 0,
    val seconds: Int = 0,
    val isRunning: Boolean = false,
    val remainingTime: Int = 0, // in seconds
    val showCompletionDialog: Boolean = false
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private val stopTimerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TimerService.ACTION_STOP_TIMER) {
                clearTimer()
            }
        }
    }

    init {
        startCountdown()
        registerStopReceiver()
    }

    private fun registerStopReceiver() {
        val filter = IntentFilter(TimerService.ACTION_STOP_TIMER)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(
                stopTimerReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            getApplication<Application>().registerReceiver(stopTimerReceiver, filter)
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerState.update { state ->
                    if (state.isRunning && state.remainingTime > 0) {
                        val newRemainingTime = state.remainingTime - 1
                        if (newRemainingTime == 0) {
                            // Start notification service with alarm
                            TimerService.startTimerNotification(getApplication())
                            state.copy(
                                isRunning = false,
                                remainingTime = 0,
                                showCompletionDialog = true
                            )
                        } else {
                            state.copy(remainingTime = newRemainingTime)
                        }
                    } else {
                        state
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(stopTimerReceiver)
        } catch (_: Exception) {
            // Receiver not registered
        }
    }

    fun setMinutes(minutes: Int) {
        _timerState.update { state ->
            val newRemainingTime = minutes * 60 + state.seconds
            state.copy(
                minutes = minutes,
                remainingTime = if (!state.isRunning) newRemainingTime else state.remainingTime
            )
        }
    }

    fun setSeconds(seconds: Int) {
        _timerState.update { state ->
            val newRemainingTime = state.minutes * 60 + seconds
            state.copy(
                seconds = seconds,
                remainingTime = if (!state.isRunning) newRemainingTime else state.remainingTime
            )
        }
    }

    fun startTimer() {
        _timerState.update { state ->
            val totalSeconds = state.minutes * 60 + state.seconds
            if (totalSeconds > 0) {
                state.copy(
                    isRunning = true,
                    remainingTime = totalSeconds
                )
            } else {
                state
            }
        }
    }

    fun pauseTimer() {
        _timerState.update { it.copy(isRunning = false) }
    }

    fun resumeTimer() {
        _timerState.update { state ->
            if (state.remainingTime > 0) {
                state.copy(isRunning = true)
            } else {
                state
            }
        }
    }

    fun togglePauseResume() {
        _timerState.update { state ->
            if (state.remainingTime > 0) {
                state.copy(isRunning = !state.isRunning)
            } else {
                state
            }
        }
    }

    fun clearTimer() {
        // Stop the notification service
        TimerService.stopTimerNotification(getApplication())
        _timerState.update {
            TimerState()
        }
    }

    fun dismissCompletionDialog() {
        // Stop the notification service when dialog is dismissed
        TimerService.stopTimerNotification(getApplication())
        _timerState.update { 
            it.copy(showCompletionDialog = false)
        }
    }
    
    fun dismissCompletionDialogAndClear() {
        // Stop the notification service
        TimerService.stopTimerNotification(getApplication())
        _timerState.update {
            TimerState()
        }
    }
}
