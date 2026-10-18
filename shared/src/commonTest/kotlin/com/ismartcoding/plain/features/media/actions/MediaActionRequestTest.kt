package com.ismartcoding.plain.features.media.actions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MediaActionRequestTest {
    @Test
    fun usesExistingMediaOperations() {
        assertEquals("trashMediaItems", mediaActionMethod(MediaAction.TRASH))
        assertEquals("restoreMediaItems", mediaActionMethod(MediaAction.RESTORE))
        assertEquals("deleteMediaItems", mediaActionMethod(MediaAction.DELETE))
        assertEquals("moveMediaItems", mediaActionMethod(MediaAction.MOVE))
    }

    @Test
    fun selectionUsesOnlyDistinctRequestedIdsAndExplicitTrashState() {
        assertEquals("ids:=17,volume:42 trash:false", mediaActionSelection(listOf("17", "volume:42", "17"), false))
        assertEquals("ids:=17 trash:true", mediaActionSelection(listOf("17"), true))
    }

    @Test
    fun rejectsEmptyAndAmbiguousCsvSelections() {
        assertFailsWith<IllegalArgumentException> { mediaActionSelection(emptyList(), false) }
        for (id in listOf("", "17,18")) {
            assertFailsWith<IllegalArgumentException>(id) { mediaActionSelection(listOf(id), false) }
        }
    }

    @Test
    fun opaqueIdsAreEscapedAsLiteralDslValues() {
        assertEquals("ids:==opaque trash:false", mediaActionSelection(listOf("=opaque"), false))
        assertEquals("ids:=17\\ all:true trash:false", mediaActionSelection(listOf("17 all:true"), false))
        assertEquals("ids:=\\\"quoted\\\"\\\\path trash:false", mediaActionSelection(listOf("\"quoted\"\\path"), false))
        assertEquals("ids:=媒体：42 trash:false", mediaActionSelection(listOf("媒体：42"), false))
        assertEquals("ids:=17\\\nall:true trash:false", mediaActionSelection(listOf("17\nall:true"), false))
    }
}
