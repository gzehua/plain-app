package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.JsonObject

internal suspend fun readUserSettings(
    read: suspend () -> JsonObject = { PlainRust.userSettings() },
): JsonObject = read()
