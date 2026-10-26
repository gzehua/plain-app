package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.File as FileEntry
import kotlinx.coroutines.CancellationException
import io.ktor.http.Url
import io.ktor.http.decodeURLPart
import kotlinx.serialization.json.*
import com.ismartcoding.plain.features.files.upload.RustUploadChunks
import com.ismartcoding.plain.lib.JsonHelper

object FilesClient {
    suspend fun size(path: String): Long = RustGraphqlApi.graphql(Query.fileInfo(path))
        .getValue("fileInfo").jsonObject.getValue("size").jsonPrimitive.long

    suspend fun writeChunks(path: String, chunks: Iterator<ByteArray>): Boolean =
        RustUploadChunks.replace(path, chunks)

    suspend fun copyToPath(srcPath: String, targetPath: String, overwrite: Boolean): Boolean =
        RustGraphqlApi.graphql(Mutation.copyFile(srcPath, targetPath, overwrite))
            .getValue("copyFile").jsonPrimitive.boolean

    suspend fun copyToDirectory(srcPath: String, dirPath: String, fileName: String): String {
        val result = RustGraphqlApi.graphql(
            Mutation.copyFileToDirectory(srcPath, dirPath, fileName),
        ).getValue("copyFileToDirectory")
        return if (result is JsonNull) "" else result.jsonPrimitive.content
    }

    suspend fun createZipArchive(paths: List<String>, target: String): Boolean = archiveOperation {
        RustGraphqlApi.graphql(Mutation.createZipArchive(paths, target)).getValue("createZipArchive").jsonPrimitive.content
    }
    suspend fun extractZipArchive(path: String, target: String): Boolean = archiveOperation {
        RustGraphqlApi.graphql(Mutation.extractZipArchive(path, target)).getValue("extractZipArchive").jsonPrimitive.content
    }

    suspend fun cacheZipEntry(path: String): String? = try {
        RustGraphqlApi.graphql(Mutation.cacheZipEntry(path)).getValue("cacheZipEntry").jsonPrimitive.content
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }

    suspend fun createDirectory(path: String): FileEntry = write("createDirectory", path)
    suspend fun createFile(path: String): FileEntry = write("createFile", path)
    suspend fun writeText(path: String, content: String, overwrite: Boolean): FileEntry = write("writeText", path, content, overwrite)

    suspend fun list(root: String, showHidden: Boolean, sortBy: FileSortBy): List<FileEntry> =
        read(root, "show_hidden:$showHidden", sortBy)

    suspend fun searchName(text: String, root: String, showHidden: Boolean, sortBy: FileSortBy): List<FileEntry> =
        read(root, "${literalNameQuery(text)} show_hidden:$showHidden", sortBy)

    suspend fun search(query: String, root: String, sortBy: FileSortBy, offset: Int = 0, limit: Int? = null): List<FileEntry> =
        read(root, query, sortBy, offset, limit)

    suspend fun indexed(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<FileEntry> =
        RustGraphqlApi.graphql(Query.indexedFiles(query, limit, offset, sortBy.name)).getValue("indexedFiles").jsonArray.map { decode(it.jsonObject) }
    suspend fun recent(): List<FileEntry> =
        JsonHelper.jsonDecodeFromElement(
            RustGraphqlApi.graphql(Query.recentFiles()).getValue("recentFiles")
        )

    suspend fun indexedCount(query: String): Int = RustGraphqlApi.graphql(Query.indexedFileCount(query)).getValue("indexedFileCount").jsonPrimitive.int

    private suspend fun archiveOperation(operation: suspend () -> String): Boolean = try {
        operation()
        true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }

    suspend fun rename(path: String, name: String): String? {
        val value = RustGraphqlApi.graphql(Mutation.renameFile(path, name)).getValue("renameFile")
        return if (value is JsonNull) null else value.jsonPrimitive.content
    }
    suspend fun delete(path: String): Boolean = deleteMany(listOf(path)) > 0

    suspend fun deleteMany(paths: List<String>): Int = RustGraphqlApi.graphql(
        Mutation.deleteFiles(paths),
    ).getValue("deleteFiles").jsonObject.getValue("affectedCount").jsonPrimitive.int

    private suspend fun read(root: String, query: String, sortBy: FileSortBy, offset: Int = 0, limit: Int? = null): List<FileEntry> =
        RustGraphqlApi.graphql(Query.files(root, query, sortBy.name, offset, limit ?: Int.MAX_VALUE))
            .getValue("files").jsonArray.map { decode(it.jsonObject) }

    internal fun literalNameQuery(text: String): String = buildString {
        append("text:=")
        for (character in text) {
            if (character == '\\' || character == '\'' || character == '"' || character.isWhitespace()) append('\\')
            append(character)
        }
    }

    private suspend fun write(action: String, path: String, content: String = "", overwrite: Boolean = false): FileEntry {
        val target = if (path.startsWith("file://")) Url(path).encodedPath.decodeURLPart() else path
        val operation = Mutation.fileWrite(action, target, content, overwrite)
        return decode(RustGraphqlApi.graphql(operation).getValue(operation.field).jsonObject)
    }

    private fun decode(row: JsonObject): FileEntry = JsonHelper.jsonDecodeFromElement(row)
}
