package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.media.MediaVolumeRevision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.*

class MediaRowsFactsTest {
    @Test
    fun revision_samples_keep_long_generations_and_original_rows() {
        val rows = JsonHelper.jsonDecode<JsonArray>("""[{"id":"track","durationMs":1234}]""")
        val revision = listOf(MediaVolumeRevision("external_primary", "version", 3_000_000_123L))
        val facts = MediaRowsFacts(rows, revision, revision)
        val encoded = JsonHelper.jsonEncodeToElement(facts).jsonObject
        assertEquals(rows, encoded.getValue("rows"))
        assertEquals(3_000_000_123L, encoded.getValue("revisionBefore").jsonArray.single().jsonObject.getValue("generation").jsonPrimitive.long)
        assertEquals(facts, JsonHelper.jsonDecodeFromElement<MediaRowsFacts>(encoded))
    }

    @Test
    fun unavailable_revision_stays_explicitly_unavailable() {
        val facts = MediaRowsFacts(emptyList(), null, null)
        val decoded = JsonHelper.jsonDecodeFromElement<MediaRowsFacts>(JsonHelper.jsonEncodeToElement(facts))
        assertEquals(null, decoded.revisionBefore)
        assertEquals(null, decoded.revisionAfter)
        assertEquals(emptyList(), decoded.rows)
    }
}
