package com.ismartcoding.plain.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class PrefActionTest {
    @Test
    fun actionsUseExistingMainSchemaOperations() {
        val cases = listOf(
            PrefAction.SetNotificationMode("allowlist") to "setNotificationFilterMode(mode: \"allowlist\") { affectedCount }",
            PrefAction.ClearNotificationApps to "clearNotificationFilterApps { affectedCount }",
        )
        cases.forEach { (action, selection) -> assertEquals(selection, action.selection()) }
    }

    @Test
    fun notificationArgumentsPreserveQuotesAndUnicodeAsOneValue() {
        val term = "a\"b\nc αβγ"
        val selection = PrefAction.SetNotificationMode(term).selection()
        val argument = selection.removePrefix("setNotificationFilterMode(mode: ").removeSuffix(") { affectedCount }")
        assertEquals(term, Json.parseToJsonElement(argument).jsonPrimitive.content)
    }
}
