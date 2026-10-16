package com.ismartcoding.plain.discover

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PairingReportTest {
    @Test
    fun requestReportPreservesProtocolValuesWithoutApplyingAppDefaults() {
        val payload = """{"fromId":"peer","timestamp":9007199254740993,"extension":{"value":null},"ips":[]}"""
        val address = "adapter\"address\n"
        val report = Json.parseToJsonElement(pairingRequestReport(payload, address)).jsonObject
        assertEquals(Json.parseToJsonElement(payload), report.getValue("request"))
        assertEquals(address, report.getValue("address").jsonPrimitive.content)
        assertFalse(report.getValue("request").jsonObject.containsKey("signature"))
        assertFalse(report.getValue("request").jsonObject.containsKey("awareSupported"))
    }
}
