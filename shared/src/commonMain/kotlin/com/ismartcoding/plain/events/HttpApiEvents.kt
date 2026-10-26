package com.ismartcoding.plain.events

import com.ismartcoding.plain.features.web.WebSettingsFeature

class HOpenWebSettingsEvent(val feature: WebSettingsFeature? = null) : HEvent()
