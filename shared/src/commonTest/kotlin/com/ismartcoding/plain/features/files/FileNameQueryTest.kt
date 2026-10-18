package com.ismartcoding.plain.features.files

import kotlin.test.Test
import kotlin.test.assertEquals

class FileNameQueryTest {
    @Test
    fun namesRemainOneLiteralTextFilter() {
        val cases = listOf(
            "white space" to "text:=white\\ space",
            "field:colon" to "text:=field:colon",
            "quote\"single'" to "text:=quote\\\"single\\'",
            "back\\slash" to "text:=back\\\\slash",
            ">greater" to "text:=>greater",
            "=equal" to "text:==equal",
            "  " to "text:=\\ \\ ",
            "needle parent:/ignored show_hidden:true" to "text:=needle\\ parent:/ignored\\ show_hidden:true",
        )
        cases.forEach { (name, query) ->
            assertEquals(query, FilesClient.literalNameQuery(name))
        }
    }
}
