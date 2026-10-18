package com.ismartcoding.plain.features.files.stored

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.api.graphql.models.AppFile
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.files.queryPickedFileInfo
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.ui.files.stored.VAppFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

object AppFileStore {
    private const val FIELDS = "id size mimeType realPath createdAt updatedAt fileName"

    fun toFidUri(fileId: String, ext: String = ""): String = if (ext.isEmpty()) "fid:$fileId" else "fid:$fileId.$ext"
    fun toFidUri(file: AppFile): String = "fid:${file.realPath.substringAfterLast('/')}"
    suspend fun importFile(srcPath: String, fileName: String = "", mimeType: String = "", deleteSrc: Boolean = false): AppFile = withIO {
        val platformResource = srcPath.startsWith("content://") || srcPath.startsWith("ph://")
        val name = if (platformResource && fileName.isEmpty()) queryPickedFileInfo(srcPath)?.displayName.orEmpty() else fileName
        importPath(srcPath, name, mimeType, deleteSrc)
    }

    private suspend fun importPath(path: String, name: String, mime: String, deleteSource: Boolean): AppFile {
        val file = withContext(NonCancellable) {
            RustContentApi.mutate("importAppFile(source: ${gql(path)}, fileName: ${gql(name)}, mimeType: ${gql(mime)}, deleteSource: $deleteSource) { $FIELDS }", longRunning = true)
                .getValue("importAppFile").file()
        }
        try {
            currentCoroutineContext().ensureActive()
            return file
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { release(file.id) }
            throw cancelled
        }
    }

    suspend fun page(offset: Int, limit: Int, query: String = ""): List<VAppFile> =
        RustContentApi.query("appFiles(offset: $offset, limit: $limit, query: ${gql(query)}) { $FIELDS }")
            .getValue("appFiles").let { JsonHelper.jsonDecodeFromElement<List<AppFile>>(it) }.map { VAppFile(it, it.fileName) }
    suspend fun count(query: String = ""): Int = RustContentApi.query("appFileCount(query: ${gql(query)})").getValue("appFileCount").jsonPrimitive.int
    suspend fun displayName(id: String): String = RustContentApi.query("appFiles(offset: 0, limit: 1, query: ${gql("text:$id")}) { fileName }")
        .getValue("appFiles").let { JsonHelper.jsonDecodeFromElement<List<AppFileName>>(it) }.firstOrNull()?.fileName.orEmpty()
    suspend fun release(id: String) {
        RustContentApi.mutate("releaseAppFile(fileId: ${gql(id.substringBefore('.'))})")
    }
    private fun JsonElement.file(): AppFile = JsonHelper.jsonDecodeFromElement<AppFile>(this)
}

@Serializable
private data class AppFileName(val fileName: String)
