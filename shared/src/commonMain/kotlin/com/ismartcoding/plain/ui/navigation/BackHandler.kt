package com.ismartcoding.plain.ui.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun PBackHandler(enabled: Boolean = true, onBack: () -> Unit)
