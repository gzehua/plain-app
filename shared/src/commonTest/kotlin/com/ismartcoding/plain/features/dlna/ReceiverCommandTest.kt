package com.ismartcoding.plain.features.dlna

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.*

class ReceiverCommandTest {
    @Test
    fun retryUsesTheExistingHttpListener() {
        assertEquals(buildJsonObject { put("action", "RETRY") }, ReceiverCommand.Retry.input())
    }

    @Test
    fun typedInputPreservesFalseFlags() {
        val rule = ReceiverCommand.RemoveRule("example", false).input()
        assertEquals("REMOVE_RULE", rule.getValue("action").jsonPrimitive.content)
        assertEquals(false, rule.getValue("allowed").jsonPrimitive.boolean)
        val accept = ReceiverCommand.Accept(false).input()
        assertEquals("ACCEPT", accept.getValue("action").jsonPrimitive.content)
        assertEquals(false, accept.getValue("remember").jsonPrimitive.boolean)
    }
}
