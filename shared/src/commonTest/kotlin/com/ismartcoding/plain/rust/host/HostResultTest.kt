package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HostResultTest {
    @Serializable
    private data class Reading(val name: String, val count: Long, val optional: String? = null)

    @Serializable(with = CountedSerializer::class)
    private data class Counted(val value: String)

    private object CountedSerializer : KSerializer<Counted> {
        var writes = 0
        override val descriptor = PrimitiveSerialDescriptor("Counted", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: Counted) {
            writes++
            encoder.encodeString(value.value)
        }
        override fun deserialize(decoder: Decoder): Counted = Counted(decoder.decodeString())
    }

    @Test
    fun dtoReplyMatchesDirectTypedSerialization() {
        val value = Reading("line\n\"quoted\"\t中文", 9_007_199_254_740_993L)
        assertEquals(JsonHelper.jsonEncode(RustHostReply(42, value)), hostResult(value).encodeReply(42))
    }

    @Test
    fun nullIsAnExplicitResultAndErrorsHaveNoResultField() {
        assertEquals("""{"id":2,"result":null}""", hostResult().encodeReply(2))
        assertEquals("""{"id":3,"result":false}""", hostResult(false).encodeReply(3))
        assertEquals("""{"id":4,"result":[]}""", hostResult(emptyList<String>()).encodeReply(4))
        assertEquals("""{"id":5,"error":"failed"}""", encodeHostError(5, Exception("failed")))
        assertEquals("""{"id":6,"error":"Host operation failed"}""", encodeHostError(6, Exception()))
    }

    @Test
    fun heterogeneousRowsKeepTheirOriginalShapes() {
        val rows = listOf(hostResult(Reading("track", 3)), hostResult(true), hostResult())
        assertEquals("""{"id":7,"result":[{"name":"track","count":3,"optional":null},true,null]}""", hostResult(rows).encodeReply(7))
    }

    @Test
    fun serializationHappensOnceWhenTheFinalReplyIsWritten() {
        CountedSerializer.writes = 0
        val result = hostResult(listOf(hostResult(Counted("a")), hostResult(Counted("b"))))
        assertEquals(0, CountedSerializer.writes)
        assertEquals("""{"id":8,"result":["a","b"]}""", result.encodeReply(8))
        assertEquals(2, CountedSerializer.writes)
    }

    @Test
    fun invalidValuesFailDuringFinalEncoding() {
        val result = hostResult(Double.NaN)
        assertFailsWith<IllegalArgumentException> { result.encodeReply(9) }
    }
}
