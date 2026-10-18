package com.ismartcoding.plain.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class UserActionTest {
    @Test
    fun actionsUseExistingMainSchemaOperations() {
        val cases = listOf(
            UserAction.RecordSearch("term") to "recordRecentSearch(query: \"term\") { affectedCount }",
            UserAction.RemoveSearch("term") to "removeRecentSearch(query: \"term\") { affectedCount }",
            UserAction.RecordSaveDirectory("/files") to "recordRecentSaveDirectory(path: \"/files\") { affectedCount }",
            UserAction.ToggleNotificationApp("package") to "toggleNotificationApp(packageName: \"package\") { affectedCount }",
            UserAction.SetNotificationMode("allowlist") to "setNotificationFilterMode(mode: \"allowlist\") { affectedCount }",
            UserAction.ClearNotificationApps to "clearNotificationFilterApps { affectedCount }",
        )
        cases.forEach { (action, selection) -> assertEquals(selection, action.selection()) }
    }

    @Test
    fun searchArgumentsPreserveQuotesAndUnicodeAsOneValue() {
        val term = "a\"b\nc αβγ"
        val selection = UserAction.RecordSearch(term).selection()
        val argument = selection.removePrefix("recordRecentSearch(query: ").removeSuffix(") { affectedCount }")
        assertEquals(term, Json.parseToJsonElement(argument).jsonPrimitive.content)
    }
}
