package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.rust.PlainRust
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.jsonObject
import com.ismartcoding.plain.lib.JsonHelper

val onlineClientIds = MutableStateFlow<Set<String>>(emptySet())

internal fun setOnlineClientIds(ids: Set<String>) { onlineClientIds.value = ids }

internal suspend fun refreshOnlineClientIds() {
    val result = PlainRust.mainQuery("onlineClientIds").getValue("onlineClientIds")
    setOnlineClientIds(JsonHelper.jsonDecodeFromElement<Set<String>>(result))
}

suspend fun closeAllWsSessions() {
    PlainRust.mainMutate("closeAllWebSocketSessions")
    onlineClientIds.value = emptySet()
}
