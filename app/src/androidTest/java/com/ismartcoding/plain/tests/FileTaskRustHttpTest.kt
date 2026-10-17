package com.ismartcoding.plain.tests

import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.preferences.RustSystemState
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FileTaskRustHttpTest {
    private suspend fun awaitTask(id: String): com.ismartcoding.plain.api.graphql.models.FileTask {
        while (true) {
            val task = checkNotNull(FileTaskClient.get(id))
            if (task.status == FileTaskStatus.DONE || task.status == FileTaskStatus.ERROR) return task
            delay(100)
        }
    }

    @Test
    fun fileTasksCopyMoveTrackActualTargetsAndReportRealFailures() = runBlocking {
        val root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"file-task-${UUID.randomUUID()}").apply { mkdirs() }
        val external = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"file-task-${UUID.randomUUID()}")
        val ids = mutableListOf<String>()
        val originalPermissions = RustSystemState.state.value.apiPermissions
        try {
            val source = File(root,"source.txt").apply { writeText("synthetic") }
            val target = File(root,"target.txt").apply { writeText("existing") }
            val second = File(root,"second.txt").apply { writeText("two") }
            val copied = FileTaskClient.create(FileTaskType.COPY,listOf(FileTaskOp(source.path,target.path),FileTaskOp(second.path,File(root,"second-copy.txt").path))).also { ids.add(it.id) }
            val done = withTimeout(20_000) { awaitTask(copied.id) }
            assertEquals(FileTaskStatus.DONE,done.status)
            assertEquals(12L,done.doneBytes)
            assertEquals(2L,done.doneItems)
            assertEquals("existing",target.readText())
            assertEquals("synthetic",File(root,"target (1).txt").readText())
            assertEquals(File(root,"target (1).txt").path,done.completedOps[0].dst)
            assertEquals(done.completedOps,FileTaskClient.get(done.id)!!.completedOps)
            assertFalse(File(root,"target (2).txt").exists())
            assertTrue(FileTaskClient.list(0,0,"").isEmpty())
            val movedRoot = File(root,"moved")
            val moved = FileTaskClient.create(FileTaskType.MOVE,listOf(FileTaskOp(File(root,"target (1).txt").path,movedRoot.path))).also { ids.add(it.id) }
            assertEquals(FileTaskStatus.DONE,withTimeout(20_000) { awaitTask(moved.id) }.status)
            assertFalse(File(root,"target (1).txt").exists())
            assertEquals("synthetic",movedRoot.readText())
            val failed = FileTaskClient.create(FileTaskType.COPY,listOf(FileTaskOp(File(root,"missing").path,File(root,"never").path))).also { ids.add(it.id) }
            val error = withTimeout(20_000) { awaitTask(failed.id) }
            assertEquals(FileTaskStatus.ERROR,error.status)
            assertTrue(error.error.isNotEmpty())
            assertTrue(error.completedOps.isEmpty())
            assertFalse(File(root,"never").exists())
            check(external.mkdirs())
            val externalCopy = FileTaskClient.create(FileTaskType.COPY,listOf(FileTaskOp(source.path,external.path))).also { ids.add(it.id) }
            assertEquals(FileTaskStatus.DONE,withTimeout(25_000) { awaitTask(externalCopy.id) }.status)
            val externalSource = File(external,source.name)
            assertEquals("synthetic",externalSource.readText())
            val externalTarget = File(external,"moved.txt")
            val externalMove = FileTaskClient.create(FileTaskType.MOVE,listOf(FileTaskOp(externalSource.path,externalTarget.path))).also { ids.add(it.id) }
            assertEquals(FileTaskStatus.DONE,withTimeout(25_000) { awaitTask(externalMove.id) }.status)
            assertFalse(externalSource.exists())
            assertEquals("synthetic",externalTarget.readText())
            var rejected = false
            try { FileTaskClient.create(FileTaskType.COPY,listOf(FileTaskOp("/system/test",File(root,"forbidden").path))) }
            catch (_: Exception) { rejected = true }
            assertTrue(rejected)
            val spoofedCaller = runCatching { RustContentApi.query("fileTask(clientId: ${JsonPrimitive("foreign-${UUID.randomUUID()}")}, id: ${JsonPrimitive(done.id)}) { id }") }
            assertTrue(spoofedCaller.isFailure)
            RustSystemState.setApiPermissions(originalPermissions + "WRITE_EXTERNAL_STORAGE")
            assertEquals(true, RustContentApi.mutate("""copyFile(src: "${source.path}", dst: "${File(root,"public-copy.txt").path}", overwrite: false)""").getValue("copyFile").jsonPrimitive.boolean)
            assertEquals("synthetic",File(root,"public-copy.txt").readText())
            var publicFailed = false
            try { RustContentApi.mutate("""copyFile(src: "/system/test", dst: "${File(root,"public-never").path}", overwrite: false)""") }
            catch (_: Exception) { publicFailed = true }
            assertTrue(publicFailed)
        } finally {
            RustSystemState.setApiPermissions(originalPermissions)
            FileTaskClient.list(0,1000,"").filter { task -> task.completedOps.any { it.src.startsWith(root.path+"/") } || task.id in ids }.forEach { FileTaskClient.remove(it.id) }
            root.deleteRecursively()
            external.deleteRecursively()
            com.ismartcoding.plain.platform.files.scanFileTaskPaths(listOf(File(external,"source.txt").path,File(external,"moved.txt").path))
        }
    }
}
