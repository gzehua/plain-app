package com.ismartcoding.plain.features.media.actions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MediaActionProjectionTest {
    @Test
    fun selectionEscapesIdsAndPreservesTrashScope() {
        assertEquals("ids:=one,two\\ words trash:true", mediaActionSelection(listOf("one", "two words", "one"), true))
        assertEquals("ids:=one trash:false", mediaActionSelection(listOf("one"), false))
        assertFailsWith<IllegalArgumentException> { mediaActionSelection(listOf("bad,id"), false) }
    }
}
