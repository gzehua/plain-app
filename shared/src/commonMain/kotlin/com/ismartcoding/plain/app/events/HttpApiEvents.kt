package com.ismartcoding.plain.app.events

import com.ismartcoding.plain.features.web.WebSettingsFeature

class HStartScreenMirrorEvent(val audio: Boolean) : HEvent()

class HRequestScreenMirrorAudioEvent : HEvent()

class HOpenAccessibilitySettingsEvent : HEvent()

class HOpenWebSettingsEvent(val feature: WebSettingsFeature? = null) : HEvent()
