package com.ismartcoding.plain.features.feed

import com.ismartcoding.plain.graphql.models.FeedEntry
import com.ismartcoding.plain.platform.chaCha20Decrypt
import com.ismartcoding.plain.preferences.RustSystemState
import kotlin.io.encoding.Base64

val FeedEntry.displayImage: String get() = decodeImage(image)

private fun decodeImage(value: String): String {
    if (value.isEmpty() || value.startsWith("http://") || value.startsWith("https://")) return value
    return chaCha20Decrypt(Base64.decode(RustSystemState.state.value.urlToken), Base64.decode(value))?.decodeToString() ?: error("Invalid feed image identifier")
}
