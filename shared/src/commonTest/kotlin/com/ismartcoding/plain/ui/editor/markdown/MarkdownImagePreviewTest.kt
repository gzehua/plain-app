package com.ismartcoding.plain.ui.editor.markdown

import com.ismartcoding.plain.ui.markdown.model.Input
import com.ismartcoding.plain.ui.markdown.model.MarkdownStateImpl
import com.ismartcoding.plain.ui.markdown.model.ReferenceLinkHandlerImpl
import com.ismartcoding.plain.ui.markdown.model.State
import kotlinx.coroutines.test.runTest
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownImagePreviewTest {
    @Test
    fun titledFeedImagesAreIncludedInThePreviewGallery() = runTest {
        assertEquals(
            listOf("fid:fixture-image"),
            imageLinks("""![Feed image](fid:fixture-image "Image title")"""),
        )
    }

    @Test
    fun previewGalleryUsesRenderedImageOrderAndResolvesReferences() = runTest {
        val content = """
            ![First](https://example.test/photo(1).jpg "Title")
            <img src="fid:fixture-html" alt="Second">
            ![Third][photo]

            [photo]: fid:fixture-reference "Reference title"
        """.trimIndent()
        assertEquals(
            listOf("https://example.test/photo(1).jpg", "fid:fixture-html", "fid:fixture-reference"),
            imageLinks(content),
        )
    }

    @Test
    fun imageExamplesInCodeAreNotPreviewImages() = runTest {
        val content = """
            `![not an image](fid:code)`

            ```markdown
            ![also not an image](fid:fence)
            ```

            ![Real image](fid:real)
        """.trimIndent()
        assertEquals(listOf("fid:real"), imageLinks(content))
    }

    private suspend fun imageLinks(content: String): List<String> {
        val flavour = GFMFlavourDescriptor()
        val state = MarkdownStateImpl(Input(content, true, flavour, MarkdownParser(flavour), ReferenceLinkHandlerImpl())).parse()
        return extractImageLinksFromMarkdown(state as State.Success)
    }
}
