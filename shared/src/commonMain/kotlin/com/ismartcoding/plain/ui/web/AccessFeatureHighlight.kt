package com.ismartcoding.plain.ui.web

import com.ismartcoding.plain.features.web.WebSettingsFeature
import kotlinx.coroutines.flow.MutableStateFlow

object AccessFeatureHighlight {
    val pending = MutableStateFlow<WebSettingsFeature?>(null)
}
