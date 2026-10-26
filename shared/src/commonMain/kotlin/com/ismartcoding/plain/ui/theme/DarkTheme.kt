package com.ismartcoding.plain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.ismartcoding.plain.i18n.*
import org.jetbrains.compose.resources.stringResource

enum class DarkTheme {
    USE_DEVICE_THEME,
    ON,
    OFF,
    ;

    @Composable
    fun getText(): String =
        when (this) {
            USE_DEVICE_THEME -> stringResource(Res.string.use_device_theme)
            ON -> stringResource(Res.string.on)
            OFF -> stringResource(Res.string.off)
        }

    companion object {
        @Composable
        @ReadOnlyComposable
        fun isDarkTheme(theme: DarkTheme): Boolean =
            when (theme) {
                USE_DEVICE_THEME -> isSystemInDarkTheme()
                ON -> true
                OFF -> false
            }
    }
}
