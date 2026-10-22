package com.ismartcoding.plain.rust

import com.ismartcoding.plain.lib.JsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class RustHttpServerResultTest {
    @Test
    fun startupCarriesTheSharedPreviewPathAsAFixedRustOwnedValue() {
        val result = JsonHelper.jsonDecode<RustHttpServerResult>(
            """{"sharedPreviewDir":"/system/cache/share_open"}""",
        )
        assertEquals("/system/cache/share_open", result.sharedPreviewDir)
        assertEquals(result, JsonHelper.jsonDecode<RustHttpServerResult>(JsonHelper.jsonEncode(result)))
    }
}
