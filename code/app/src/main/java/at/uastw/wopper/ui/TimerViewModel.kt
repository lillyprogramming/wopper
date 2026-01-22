package at.uastw.wopper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    init {
        startCountdown()
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerState.update { state ->
                    if (state.isRunning && state.remainingTime > 0) {
                        val newRemainingTime = state.remainingTime - 1
                        if (newRemainingTime == 0) {
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
        _timerState.update {
            TimerState()
        }
    }

    fun dismissCompletionDialog() {
        _timerState.update { 
            it.copy(showCompletionDialog = false)
        }
    }
    
    fun dismissCompletionDialogAndClear() {
        _timerState.update {
            TimerState()
        }
    }
}
