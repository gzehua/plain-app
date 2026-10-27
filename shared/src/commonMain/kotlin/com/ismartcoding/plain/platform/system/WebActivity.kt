package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.TimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal object WebActivity {
    private val lastRequestMs = MutableStateFlow(0L)
    val lastRequest = lastRequestMs.asStateFlow()
    fun received() { lastRequestMs.value = TimeHelper.nowMillis() }
}
