package com.ismartcoding.plain.ui.components

import androidx.compose.runtime.compositionLocalOf

val LocalOpenDrawer = compositionLocalOf<(() -> Unit)?> { null }
