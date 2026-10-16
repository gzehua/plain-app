package com.ismartcoding.plain.tests

import com.ismartcoding.plain.tests.fixtures.ImageEmbeddings

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.tags.TagRelationStub
import com.ismartcoding.plain.tests.fixtures.imageEmbedding
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.*
import com.ismartcoding.plain.ui.permissions.*
import com.ismartcoding.plain.features.bookmarks.*
import com.ismartcoding.plain.features.chat.*
import com.ismartcoding.plain.ui.chat.*
import com.ismartcoding.plain.features.clipboard.*
import com.ismartcoding.plain.ui.clipboard.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.images.editor.*
import com.ismartcoding.plain.ui.images.editor.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.permissions.*
import com.ismartcoding.plain.ui.permissions.*
import com.ismartcoding.plain.features.pomodoro.*
import com.ismartcoding.plain.ui.pomodoro.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.features.videos.*
import com.ismartcoding.plain.ui.videos.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.platform.files.renameAndScanFile
import com.ismartcoding.plain.platform.files.deleteFileOrDir
import com.ismartcoding.plain.platform.files.queryFileTaskMedia
import com.ismartcoding.plain.platform.files.scanFileTaskPaths
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FileRenameDeleteMediaRustHttpTest {
    @Test
    fun ordinaryRenameAndDeleteRebindThenCleanRealImageAndFileMetadata() = runBlocking {
        val prefix = "synthetic-file-media-${UUID.randomUUID()}"
        val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), prefix)
        val target = File(root, "target").apply { check(mkdirs()) }
        val resolver = appContext.contentResolver
        val tags = mutableListOf<String>()
        val ids = mutableSetOf<String>()
        var uri: Uri? = null
        var sourcePath = ""
        var destinationPath = ""
        try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$prefix.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/$prefix/source/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            checkNotNull(resolver.openOutputStream(uri)).use { output ->
                val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                try { bitmap.eraseColor(0xff225588.toInt()); check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) }
                finally { bitmap.recycle() }
            }
            values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            assertEquals(1, resolver.update(uri, values, null, null))
            val oldId = checkNotNull(uri.lastPathSegment).also { ids.add(it) }
            sourcePath = checkNotNull(resolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)).use {
                check(it.moveToFirst()); checkNotNull(it.getString(0))
            }
            destinationPath = File(File(sourcePath).parentFile, "renamed.png").path
            val imageTag = TagsClient.addOrUpdate("") { name = prefix; type = DataType.IMAGE.value }.also { tags.add(it) }
            val fileTag = TagsClient.addOrUpdate("") { name = prefix; type = DataType.FILE.value }.also { tags.add(it) }
            TagsClient.editTagRelations(DataType.IMAGE, TagRelationStub(oldId, prefix, 1), listOf(imageTag), emptyList())
            TagsClient.editTagRelations(DataType.FILE, TagRelationStub(sourcePath, prefix, 1), listOf(fileTag), emptyList())
            ImageEmbeddings.insertAll(listOf(imageEmbedding(oldId, sourcePath, ByteBuffer.allocate(4).putFloat(1f).array())))
            assertNull(renameAndScanFile(sourcePath, "../escape.png"))
            val collision = File(File(sourcePath).parentFile, "existing.png").apply { writeText("existing") }
            assertNull(renameAndScanFile(sourcePath, collision.name))
            assertEquals("existing", collision.readText())
            assertEquals(destinationPath, renameAndScanFile(sourcePath, "renamed.png"))
            val renameTask = FileTaskHelper.list(0, 1000, "").single { task -> task.completedOps.any { it.src == sourcePath && it.dst == destinationPath } }
            assertEquals(FileTaskType.MOVE, renameTask.type)
            assertEquals(FileTaskStatus.DONE, renameTask.status)
            assertEquals(FileTaskStatus.DONE, FileTaskHelper.get(renameTask.id)!!.status)
            assertFalse(File(sourcePath).exists())
            assertTrue(File(destinationPath).isFile)
            val moved = queryFileTaskMedia(listOf(destinationPath)).single()
            assertEquals(DataType.IMAGE, moved.type)
            ids.add(moved.id)
            assertEquals(listOf(imageTag), TagsClient.getTagRelationsByKey(moved.id, DataType.IMAGE).map { it.tagId })
            if (oldId != moved.id) assertTrue(TagsClient.getTagRelationsByKey(oldId, DataType.IMAGE).isEmpty())
            assertTrue(TagsClient.getTagRelationsByKey(sourcePath, DataType.FILE).isEmpty())
            assertEquals(listOf(fileTag), TagsClient.getTagRelationsByKey(destinationPath, DataType.FILE).map { it.tagId })
            assertFalse(ImageEmbeddings.getAllIds().any { it in ids })
            ImageEmbeddings.insertAll(listOf(imageEmbedding(moved.id, destinationPath, ByteBuffer.allocate(4).putFloat(1f).array())))
            assertTrue(deleteFileOrDir(destinationPath))
            assertFalse(File(destinationPath).exists())
            assertTrue(TagsClient.getTagRelationsByKey(moved.id, DataType.IMAGE).isEmpty())
            assertTrue(TagsClient.getTagRelationsByKey(destinationPath, DataType.FILE).isEmpty())
            assertFalse(ImageEmbeddings.getAllIds().any { it in ids })
            assertFalse(deleteFileOrDir(destinationPath))
        } finally {
            FileTaskHelper.list(0, 1000, "").filter { task -> task.completedOps.any { it.src.startsWith(root.path + "/") } }.forEach { FileTaskHelper.remove(it.id) }
            tags.forEach { TagsClient.delete(it) }
            ImageEmbeddings.deleteByIds(ids.toList())
            uri?.let { resolver.delete(it, null, null) }
            ids.forEach { id -> resolver.delete(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), null, null) }
            root.deleteRecursively()
            scanFileTaskPaths(listOf(sourcePath, destinationPath, File(root, "source").path, target.path, root.path).filter { it.isNotEmpty() })
        }
    }
}
