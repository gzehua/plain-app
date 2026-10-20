package com.ismartcoding.plain.tests

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.ismartcoding.plain.ui.base.AlertType
import com.ismartcoding.plain.ui.base.PAlert
import com.ismartcoding.plain.ui.base.PTextButton
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PAlertLayoutTest {
    @get:Rule
    val activityRule = ActivityTestRule(ComponentActivity::class.java)

    private var cardBounds = Rect.Zero
    private var density = 1f

    @Test
    fun singleLineTitleIsVerticallyCentered() {
        show(withActions = false)
        onMain {
            val title = textNode("Alert title").boundsInRoot
            assertEquals(cardBounds.center.y, title.center.y, 1f)
            assertEquals(56 * density, cardBounds.height, 1f)
        }
    }

    @Test
    fun actionsStayCloseToTheBottomEdge() {
        show(withActions = true)
        onMain {
            val button = textNode("Fix").boundsInRoot
            val bottomGap = (cardBounds.bottom - button.bottom) / density
            val topGap = (textNode("Alert title").boundsInRoot.top - cardBounds.top) / density
            assertTrue("bottom gap $bottomGap vs top gap $topGap", bottomGap <= topGap + 4)
        }
    }

    private fun show(withActions: Boolean) {
        val laidOut = CountDownLatch(1)
        onMain {
            activityRule.activity.setContent {
                density = LocalDensity.current.density
                AppTheme(useDarkTheme = false) {
                    PAlert(
                        description = "Alert title",
                        type = AlertType.INFO,
                        modifier = Modifier.onGloballyPositioned {
                            cardBounds = it.boundsInRoot()
                            laidOut.countDown()
                        },
                        actions = if (withActions) ({ PTextButton(text = "Fix", buttonSize = ButtonSize.SMALL, onClick = {}) }) else null,
                    )
                }
            }
        }
        assertTrue("Layout did not complete", laidOut.await(5, TimeUnit.SECONDS))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun textNode(text: String): SemanticsNode =
        roots(activityRule.activity.window.decorView)
            .flatMap { descendants(it.semanticsOwner.unmergedRootSemanticsNode) }
            .first { it.config.getOrNull(SemanticsProperties.Text)?.singleOrNull()?.text == text }

    private fun descendants(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { descendants(it) }

    private fun roots(view: View): List<ViewRootForTest> = when (view) {
        is ViewRootForTest -> listOf(view)
        is ViewGroup -> (0 until view.childCount).flatMap { roots(view.getChildAt(it)) }
        else -> emptyList()
    }

    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
}
