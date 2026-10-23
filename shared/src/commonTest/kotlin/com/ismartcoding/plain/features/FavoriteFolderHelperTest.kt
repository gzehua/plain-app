package com.ismartcoding.plain.features

import com.ismartcoding.plain.models.FavoriteFolder
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FavoriteFolderHelperTest {

    private fun decode(value: String): List<FavoriteFolder> =
        JsonHelper.jsonDecodeFromElement(Json.parseToJsonElement(value).jsonObject.values.single())

    private val wholeList =
        """
        {"addFavoriteFolder":[
          {"rootPath":"内部存储","fullPath":"内部存储/Documents","alias":"工作"},
          {"rootPath":"内部存储","fullPath":"内部存储/Pictures","alias":null}
        ]}
        """.trimIndent()

    @Test
    fun decodes_whole_list_returned_by_mutations() {
        assertEquals(
            listOf("内部存储/Documents", "内部存储/Pictures"),
            decode(wholeList).map { it.fullPath },
        )
    }

    @Test
    fun reads_camelCase_fields_from_shared_resolver() {
        val folder = decode(wholeList).first()
        assertEquals("内部存储", folder.rootPath)
        assertEquals("内部存储/Documents", folder.fullPath)
        assertEquals("工作", folder.alias)
    }

    @Test
    fun keeps_absent_alias_null() {
        assertNull(decode(wholeList)[1].alias)
    }

    @Test
    fun decodes_empty_list() {
        assertEquals(emptyList<FavoriteFolder>(), decode("""{"removeFavoriteFolder":[]}"""))
    }

    @Test
    fun escapes_quotes_and_backslashes_in_paths() {
        assertEquals("\"内部存储/Documents\"", JsonHelper.jsonEncode("内部存储/Documents"))
        assertEquals(""""/sdcard/\"My\" Folder"""", JsonHelper.jsonEncode("/sdcard/\"My\" Folder"))
        assertEquals(""""/sdcard/a\\b"""", JsonHelper.jsonEncode("/sdcard/a\\b"))
    }

    @Test
    fun escapes_newline_so_arguments_stay_on_one_line() {
        val argument = JsonHelper.jsonEncode("a\nb")
        assertTrue(argument.contains("\\n"))
        assertFalse(argument.contains("\n"))
    }
}
