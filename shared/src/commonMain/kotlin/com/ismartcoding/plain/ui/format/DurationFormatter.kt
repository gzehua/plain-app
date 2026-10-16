package com.ismartcoding.plain.ui.format

import androidx.compose.runtime.Composable
import com.ismartcoding.plain.i18n.*
import org.jetbrains.compose.resources.PluralStringResource

object DurationFormatter {
    @Composable
    fun formatSeconds(n: Int, plural: @Composable (PluralStringResource, Int, Int) -> String): String {
        val seconds = n % 60
        val minutes = n / 60 % 60
        val hours = n / 3600
        var r = ""
        if (hours > 0) {
            r += plural(Res.plurals.n_hours, hours, hours)
        }

        if (minutes > 0) {
            r += plural(Res.plurals.n_minutes, minutes, minutes)
        }

        if (seconds > 0) {
            r += plural(Res.plurals.n_seconds, seconds, seconds)
        }

        return r.trimEnd()
    }

}
