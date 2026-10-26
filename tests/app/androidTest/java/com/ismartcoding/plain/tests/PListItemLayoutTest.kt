package com.ismartcoding.plain.tests

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.settings
import com.ismartcoding.plain.ui.theme.AppTheme
import com.ismartcoding.plain.ui.theme.listItemTitle
import com.ismartcoding.plain.ui.theme.listItemSubtitle
import com.ismartcoding.plain.ui.theme.listItemDescription
import com.ismartcoding.plain.ui.theme.listItemValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PListItemLayoutTest {
    @get:Rule
    val activityRule = ActivityTestRule(ComponentActivity::class.java)

    private var rowBounds = Rect.Zero
    private var pixelsPerDp = 1f

    @Test
    fun widthRelationsPreserveTheWholeTitle() {
        for ((title, value) in listOf("Setting title" to "OK", "ID" to "Selected value", "Title" to "Value")) {
            for (ratio in listOf(0.75f, 1f, 1.25f)) {
                show(title, value, LayoutDirection.Ltr, 1f, widthRatio = ratio)
                onMain {
                    assertComplete(textNode(nodes(), title))
                    assertComplete(textNode(nodes(), value))
                    assertEquals(1, textLayout(textNode(nodes(), title)).lineCount)
                }
            }
        }
    }

    @Test
    fun titleAndValueFitWithoutClippingInBothDirectionsAndLargeText() {
        for (direction in LayoutDirection.entries) {
            for (fontScale in listOf(1f, 2f)) {
                for ((title, value) in listOf(
                    "Longer title" to "OK",
                    "ID" to "Longer value",
                    "Title" to "Value",
                    "Longer setting title" to "A value that exceeds the remaining width",
                    "ID" to "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                    "A longer title" to "A longer value",
                    "A title that is longer than the entire list item width" to "Complete value",
                    "多语言标题需要完整显示" to "这是需要在剩余宽度内自动换行的详细内容",
                )) {
                    show(title, value, direction, fontScale)
                    onMain {
                        val nodes = nodes()
                        val titleNode = textNode(nodes, title)
                        val valueNode = textNode(nodes, value)
                        assertComplete(titleNode)
                        assertComplete(valueNode)
                        val titleBounds = titleNode.boundsInRoot
                        val valueBounds = valueNode.boundsInRoot
                        assertFalse("Text overlaps: $title / $value", titleBounds.overlaps(valueBounds))
                        assertTrue(titleBounds.left >= rowBounds.left && titleBounds.right <= rowBounds.right + 1)
                        assertTrue(valueBounds.left >= rowBounds.left && valueBounds.right <= rowBounds.right + 1)
                    }
                }
            }
        }
    }

    @Test
    fun valueWithSubtitleIconAndChevronRemainsComplete() {
        for (direction in LayoutDirection.entries) {
            show("A longer setting title", "A complete value that needs to wrap", direction, 1f, accessories = true)
            onMain {
                val nodes = nodes()
                assertComplete(textNode(nodes, "A longer setting title"))
                assertComplete(textNode(nodes, "A complete value that needs to wrap"))
                assertComplete(textNode(nodes, "Supporting text"))
            }
        }
    }

    @Test
    fun emptyValueAndActionKeepTheirContentWidth() {
        show("Switch action", "", LayoutDirection.Ltr, 1f, switch = true)
        onMain {
            val nodes = nodes()
            assertComplete(textNode(nodes, "Switch action"))
            val switch = nodes.single { it.config.getOrNull(SemanticsProperties.ToggleableState) != null }
            val title = textNode(nodes, "Switch action")
            assertTrue(switch.boundsInRoot.left >= title.boundsInRoot.right)
            assertTrue(switch.boundsInRoot.width > 0)
        }
    }

    @Test
    fun valueDoesNotChangeRowHeight() {
        for (accessories in listOf(false, true)) {
            show("Notifications", "", LayoutDirection.Ltr, 1f, accessories = accessories)
            val withoutValue = rowBounds.height
            show("Notifications", "Enabled", LayoutDirection.Ltr, 1f, accessories = accessories)
            assertEquals("accessories=$accessories", withoutValue, rowBounds.height, 0.5f)
        }
    }

    @Test
    fun richRowsPreserveTextActionsAndSemanticsInBothDirections() {
        val title = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Internet") }
            append(" via Office")
        }
        for (direction in LayoutDirection.entries) {
            for (fontScale in listOf(1f, 2f)) {
                var clicks = 0
                var expectedStyles = emptyList<TextStyle>()
                show("", "", direction, fontScale, customContent = {
                    expectedStyles = listOf(MaterialTheme.typography.listItemTitle(),
                        MaterialTheme.typography.listItemSubtitle(), MaterialTheme.typography.listItemDescription())
                    PListItem(
                        modifier = Modifier.testTag("rich-row").clickable { clicks++ },
                        title = { Text(title) },
                        subtitle = { Text("Every day 22:00–07:00") },
                        description = { Text("Private route") },
                        start = { Box(Modifier.size(24.dp).testTag("leading")) },
                        action = { Switch(checked = true, onCheckedChange = {}) },
                    )
                })
                onMain {
                    val nodes = nodes()
                    val lines = listOf(title.text, "Every day 22:00–07:00", "Private route").map { textNode(nodes, it) }
                    lines.forEach(::assertComplete)
                    lines.zip(expectedStyles).forEach { (node, expected) ->
                        val actual = textLayout(node).layoutInput.style
                        assertEquals(expected.fontSize, actual.fontSize)
                        assertEquals(expected.fontWeight, actual.fontWeight)
                        assertEquals(expected.color, actual.color)
                    }
                    assertEquals(title, textLayout(lines.first()).layoutInput.text)
                    lines.zipWithNext().forEach { (before, after) ->
                        assertEquals(4 * pixelsPerDp, after.boundsInRoot.top - before.boundsInRoot.bottom, 1f)
                    }
                    val leading = nodes.single { it.config.getOrNull(SemanticsProperties.TestTag) == "leading" }
                    val action = nodes.single { it.config.getOrNull(SemanticsProperties.ToggleableState) != null }
                    lines.forEach {
                        assertFalse(it.boundsInRoot.overlaps(leading.boundsInRoot))
                        assertFalse(it.boundsInRoot.overlaps(action.boundsInRoot))
                        assertTrue(it.boundsInRoot.left >= rowBounds.left && it.boundsInRoot.right <= rowBounds.right + 1)
                    }
                    assertFalse(leading.boundsInRoot.overlaps(action.boundsInRoot))
                    val row = nodes.single { it.config.getOrNull(SemanticsProperties.TestTag) == "rich-row" }
                    assertTrue(row.config.isMergingSemanticsOfDescendants)
                    assertTrue(row.config.getOrNull(SemanticsActions.OnClick)?.action?.invoke() == true)
                    assertEquals(1, clicks)
                }
            }
        }
    }

    @Test
    fun richRowsWithoutSupportingContentHaveNoEmptyGapsAndRespectMinimumHeight() {
        for (minimum in listOf(0.dp, 64.dp)) {
            show("", "", LayoutDirection.Ltr, 1f, customContent = {
                PListItem(title = { Text("Short title") }, minHeight = minimum)
            })
            onMain {
                val texts = nodes().filter { it.config.getOrNull(SemanticsProperties.Text) != null }
                assertEquals(1, texts.size)
                val expected = maxOf(minimum.value * pixelsPerDp, texts.single().boundsInRoot.height + 16 * pixelsPerDp)
                assertEquals(expected, rowBounds.height, 1f)
            }
        }
    }

    private fun show(title: String, value: String, direction: LayoutDirection, fontScale: Float, switch: Boolean = false, widthRatio: Float? = null, accessories: Boolean = false, customContent: (@Composable () -> Unit)? = null) {
        val laidOut = CountDownLatch(1)
        onMain {
            activityRule.activity.setContent {
                val density = LocalDensity.current
                pixelsPerDp = density.density
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale),
                    LocalLayoutDirection provides direction,
                ) {
                    AppTheme(useDarkTheme = false) {
                        val measurer = rememberTextMeasurer()
                        val width = if (widthRatio != null) {
                            val titleWidth = measurer.measure(title, MaterialTheme.typography.listItemTitle(), softWrap = false).size.width
                            val valueWidth = measurer.measure(value, MaterialTheme.typography.listItemValue(), softWrap = false).size.width
                            with(density) { ((titleWidth + valueWidth) * widthRatio).toDp() } + 48.dp
                        } else 280.dp
                        Column(Modifier.width(width).onGloballyPositioned {
                            rowBounds = it.boundsInRoot()
                            laidOut.countDown()
                        }) {
                            if (customContent != null) customContent() else PListItem(
                                title = title,
                                value = value,
                                subtitle = if (accessories) "Supporting text" else "",
                                icon = if (accessories) UiRes.drawable.settings else null,
                                showMore = accessories,
                                action = if (switch) ({ Switch(checked = true, onCheckedChange = {}) }) else null,
                            )
                        }
                    }
                }
            }
        }
        assertTrue("Layout did not complete", laidOut.await(5, TimeUnit.SECONDS))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onMain { roots(activityRule.activity.window.decorView).forEach { it.measureAndLayoutForTest() } }
    }

    private fun textLayout(node: SemanticsNode): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action
        assertTrue(action?.invoke(results) == true)
        return results.single()
    }

    private fun assertComplete(node: SemanticsNode) {
        val result = textLayout(node)
        // Simple-text semantics recreates paragraphs at maxWidth instead of the measured width.
        for (line in 0 until result.lineCount) {
            val paintedWidth = result.getLineRight(line) - result.getLineLeft(line)
            assertTrue("Text exceeds measured width: ${result.layoutInput.text}", paintedWidth <= result.size.width)
        }
        assertFalse("Text is clipped vertically: ${result.layoutInput.text}", result.didOverflowHeight)
        assertEquals(result.layoutInput.text.length, result.getLineEnd(result.lineCount - 1))
    }

    private fun textNode(nodes: List<SemanticsNode>, text: String): SemanticsNode =
        nodes.single { it.config.getOrNull(SemanticsProperties.Text)?.singleOrNull()?.text == text }

    private fun nodes(): List<SemanticsNode> = roots(activityRule.activity.window.decorView)
        .flatMap { descendants(it.semanticsOwner.unmergedRootSemanticsNode) }

    private fun descendants(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { descendants(it) }

    private fun roots(view: View): List<ViewRootForTest> = when (view) {
        is ViewRootForTest -> listOf(view)
        is ViewGroup -> (0 until view.childCount).flatMap { roots(view.getChildAt(it)) }
        else -> emptyList()
    }

    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
}
