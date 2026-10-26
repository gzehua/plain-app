package com.ismartcoding.plain.app.events

import com.ismartcoding.plain.events.HEvent

class HStartScreenMirrorEvent(val audio: Boolean) : HEvent()

class HRequestScreenMirrorAudioEvent : HEvent()

class HOpenAccessibilitySettingsEvent : HEvent()
