package com.ismartcoding.plain.httpserver

import com.ismartcoding.plain.platform.system.BatterySample
import com.ismartcoding.plain.lib.JsonHelper.jsonEncode
import kotlin.test.Test
import kotlin.test.assertTrue

class SystemSamplesSerializationTest {
    @Test
    fun rawBatterySampleEncodesWithoutReportConversion() {
        val json = jsonEncode(BatterySample(level = 3.0, scale = 4.0, charging = false, temperatureTenths = 235))
        assertTrue(json.contains("\"level\":3.0"), json)
        assertTrue(json.contains("\"scale\":4.0"), json)
        assertTrue(json.contains("\"temperatureTenths\":235"), json)
    }
}
