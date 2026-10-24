package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals

class PrefTest {
    private enum class Mode(val code: Int) { FIRST(20), SECOND(10) }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun committedSnapshotsPublishDistinctValuesWithoutWritingToRust() = runTest {
        val pref = Pref("count", serializer<Int>(), 3)
        val observed = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            pref.collect { observed += it }
        }
        pref.restore(4)
        pref.restore(4)
        pref.restore(5)
        assertEquals(listOf(3, 4, 5), observed)
        assertEquals(5, pref.value)
        assertEquals(listOf(5), pref.replayCache)
    }

    @Test
    fun snapshotDecodingPreservesTheFallbackForMissingOrInvalidValues() {
        val pref = Pref("count", serializer<Int>(), 3)
        assertEquals(8, pref.decode(JsonHelper.jsonDecode("8"), 3))
        assertEquals(3, pref.decode(null, 3))
        assertEquals(3, pref.decode(JsonHelper.jsonDecode("\"invalid\""), 3))
        assertEquals(3, pref.value)
    }

    @Test
    fun enumCodesPreserveExplicitCodesOrdinalsAndUnknownValueDefaults() {
        val coded = enumCodeSerializer(Mode.FIRST) { it.code }
        assertEquals("10", JsonHelper.json.encodeToString(coded, Mode.SECOND))
        assertEquals(Mode.SECOND, JsonHelper.json.decodeFromString(coded, "10"))
        assertEquals(Mode.FIRST, JsonHelper.json.decodeFromString(coded, "999"))
        val ordinal = enumCodeSerializer(Mode.FIRST) { it.ordinal }
        assertEquals("1", JsonHelper.json.encodeToString(ordinal, Mode.SECOND))
        assertEquals(Mode.SECOND, JsonHelper.json.decodeFromString(ordinal, "1"))
    }
}
