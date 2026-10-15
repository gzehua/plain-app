package com.ismartcoding.plain.platform

import kotlinx.serialization.json.JsonElement

internal expect fun readSystemProperties(names: List<String>): Map<String, JsonElement>
