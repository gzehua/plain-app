package com.ismartcoding.plain.tests

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.ismartcoding.plain.ui.base.PInputChip
import com.ismartcoding.plain.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PInputChipLayoutTest {
    @get:Rule
    val activityRule = ActivityTestRule(ComponentActivity::class.java)

    private var heightDp = 0f

    @Test
    fun removeOnlyChipKeepsStandardHeight() {
        show(onClick = null)
        assertEquals(32f, heightDp, 0.5f)
    }

    @Test
    fun removableChipWithPrimaryActionKeepsStandardHeight() {
        show(onClick = {})
        assertEquals(32f, heightDp, 0.5f)
    }

    private fun show(onClick: (() -> Unit)?) {
        val laidOut = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activityRule.activity.setContent {
                val density = LocalDensity.current.density
                AppTheme(useDarkTheme = false) {
                    PInputChip(
                        text = "Removable chip",
                        onClick = onClick,
                        onClose = {},
                        modifier = Modifier.onGloballyPositioned {
                            heightDp = it.size.height / density
                            laidOut.countDown()
                        },
                    )
                }
            }
        }
        assertTrue("Layout did not complete", laidOut.await(5, TimeUnit.SECONDS))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
