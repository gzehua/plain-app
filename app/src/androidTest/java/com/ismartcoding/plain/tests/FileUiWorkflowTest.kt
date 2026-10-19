package com.ismartcoding.plain.tests

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ismartcoding.plain.MainActivity
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.files.FavoriteFoldersClient
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.platform.files.getInternalStoragePath
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileUiWorkflowTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Test
    fun creates_a_folder_through_the_favorite_directory_and_checks_the_actual_filesystem() = runBlocking {
        check(appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
        check(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish")
        check(InstrumentationRegistry.getArguments().getString("plainUiFixture") == "isolated")
        val root = File(getInternalStoragePath(), "Download/PlainUiFixture/${UUID.randomUUID()}")
        val alias = "UI fixture"
        val folderName = "folder lowercase"
        var created = false
        var favoriteAdded = false
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                try {
                    click(getString(Res.string.onboarding_skip))
                    assertTrue(root.mkdirs())
                    created = true
                    FavoriteFoldersClient.add(getInternalStoragePath(), root.path)
                    favoriteAdded = true
                    FavoriteFoldersClient.rename(root.path, alias)
                    click(getString(Res.string.tools))
                    click(getString(Res.string.files))
                    click(getString(Res.string.folders))
                    click(alias)
                    click(getString(Res.string.more))
                    click(getString(Res.string.create_folder))
                    val input = awaitNode { it.isEditable }
                    val arguments = Bundle().apply {
                        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, folderName)
                    }
                    assertTrue(input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments))
                    click(getString(Res.string.confirm))
                    awaitNode { it.text?.toString() == folderName && !it.isEditable && it.className == "android.widget.TextView" }
                    assertTrue(File(root, folderName).isDirectory)
                    assertTrue(root.listFiles()!!.map { it.name } == listOf(folderName))
                } finally {
                    instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                        try {
                            File(appContext.cacheDir, "ui-file-workflow.png").outputStream().use { output ->
                                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                            }
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }
        } finally {
            if (favoriteAdded) FavoriteFoldersClient.remove(root.path)
            if (created) assertTrue(root.deleteRecursively())
        }
    }

    private fun click(label: String) {
        instrumentation.uiAutomation.waitForIdle(300, 5000)
        var node = awaitNode { it.isVisibleToUser && it.isEnabled && (it.text?.toString() == label || it.contentDescription?.toString() == label) }
        repeat(16) {
            if (node.isClickable) {
                assertTrue("Control is not visible: $label", node.isVisibleToUser)
                assertTrue("Click failed: $label", node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                return
            }
            node = checkNotNull(node.parent) { "Control has no clickable parent: $label" }
        }
        error("Control parent limit reached: $label")
    }

    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        repeat(300) {
            instrumentation.uiAutomation.rootInActiveWindow?.let { root ->
                find(root, predicate)?.let { return it }
            }
            SystemClock.sleep(100)
        }
        error("Expected UI control did not become available")
    }

    private fun find(node: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        repeat(node.childCount) { index ->
            node.getChild(index)?.let { child -> find(child, predicate)?.let { return it } }
        }
        return null
    }
}
