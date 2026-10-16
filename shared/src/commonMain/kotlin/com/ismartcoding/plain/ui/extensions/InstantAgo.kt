package com.ismartcoding.plain.ui.extensions

import androidx.compose.runtime.Composable
import com.ismartcoding.plain.ui.format.RelativeTimeFormatter
import kotlin.time.Instant

@Composable
fun Instant.timeAgo(): String {
    return RelativeTimeFormatter.format(toEpochMilliseconds())
}
