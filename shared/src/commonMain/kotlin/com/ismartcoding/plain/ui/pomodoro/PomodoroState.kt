package com.ismartcoding.plain.ui.pomodoro

import androidx.compose.runtime.Composable
import com.ismartcoding.plain.models.PomodoroState
import com.ismartcoding.plain.i18n.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun PomodoroState.getText(): String = when (this) {
    PomodoroState.WORK -> stringResource(Res.string.work_time)
    PomodoroState.SHORT_BREAK -> stringResource(Res.string.short_break)
    PomodoroState.LONG_BREAK -> stringResource(Res.string.long_break)
}
