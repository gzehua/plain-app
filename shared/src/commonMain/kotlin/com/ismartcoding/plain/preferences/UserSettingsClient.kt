package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.request.NativeManagementClient
import kotlinx.serialization.json.JsonObject

internal suspend fun readUserSettings(
    read: suspend () -> JsonObject = { NativeManagementClient.userSettings() },
): JsonObject = read()
