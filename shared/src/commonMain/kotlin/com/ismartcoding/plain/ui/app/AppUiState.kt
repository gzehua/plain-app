package com.ismartcoding.plain.ui.app

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf

object AppUiState {
    val ip4s = mutableStateOf(emptyList<String>())
    val videoPlayProgressMap = mutableStateMapOf<String, Long>()
}
