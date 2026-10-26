package com.ismartcoding.plain.events

import com.ismartcoding.plain.lib.ChannelEvent

class InstallAppUpdateEvent(val path: String) : ChannelEvent()

class AddMediaShortcutEvent(val path: String, val label: String) : ChannelEvent()
