package com.ismartcoding.plain.features.notes

import kotlinx.serialization.json.*
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.Note

internal fun JsonElement.note(): Note = JsonHelper.jsonDecodeFromElement<Note>(this)
