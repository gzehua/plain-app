package com.ismartcoding.plain.ui.platform.system

import androidx.compose.runtime.Composable

@Composable
expect fun rememberClickFeedback(): (isHaptic: Boolean, isSound: Boolean) -> Unit
