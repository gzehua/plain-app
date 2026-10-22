package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.native.RustNativeApi

import kotlinx.serialization.json.JsonObject

internal suspend fun readUserSettings(
    read: suspend () -> JsonObject = { RustNativeApi.userSettings() },
): JsonObject = read()
