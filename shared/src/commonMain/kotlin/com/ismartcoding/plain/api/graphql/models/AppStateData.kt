package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.models.FeedSyncState
import com.ismartcoding.plain.models.PomodoroToday
import kotlinx.serialization.Serializable

@Serializable
internal data class FeedSyncUpdate(val states: List<FeedSyncState>, val removed: List<String>)
@Serializable
internal data class AudioPlaybackChange(val revision: Long, val positionMs: Long)
@Serializable
internal data class PeerTransportState(val peerId: String, val transport: String)
@Serializable
internal data class PairingSuccessNotice(val deviceId: String, val key: String)
@Serializable
internal data class PomodoroAction(val today: PomodoroToday, val completedState: String? = null)
