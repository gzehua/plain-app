package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.rust.graphql.gql
import kotlinx.coroutines.CancellationException
import io.ktor.http.Url
import io.ktor.http.decodeURLPart
import kotlinx.serialization.json.*

object FilesClient {
    suspend fun size(path: String): Long = RustGraphqlApi.query("fileInfo(path: ${gql(path)}) { size }")
        .getValue("fileInfo").jsonObject.getValue("size").jsonPrimitive.long

    suspend fun writeChunks(path: String, chunks: Iterator<ByteArray>): Boolean =
        com.ismartcoding.plain.features.files.upload.RustUploadChunks.replace(path, chunks)

    suspend fun copyToPath(srcPath: String, targetPath: String, overwrite: Boolean): Boolean =
        RustGraphqlApi.mutate("copyFile(src: ${gql(srcPath)}, dst: ${gql(targetPath)}, overwrite: $overwrite)", longRunning = true)
            .getValue("copyFile").jsonPrimitive.boolean

    suspend fun copyToDirectory(srcPath: String, dirPath: String, fileName: String): String {
        val result = RustGraphqlApi.graphql(
            "mutation(\$src:String!,\$directory:String!,\$name:String!){copyFileToDirectory(src:\$src,directory:\$directory,name:\$name)}",
            mapOf("src" to JsonPrimitive(srcPath), "directory" to JsonPrimitive(dirPath), "name" to JsonPrimitive(fileName)),
            longRunning = true,
        ).getValue("copyFileToDirectory")
        return if (result is JsonNull) "" else result.jsonPrimitive.content
    }

    suspend fun createZipArchive(paths: List<String>, target: String): Boolean = archiveOperation {
        RustGraphqlApi.mutate("createZipArchive(paths: [${paths.joinToString(",") { gql(it) }}], target: ${gql(target)})", longRunning = true).getValue("createZipArchive").jsonPrimitive.content
    }
    suspend fun extractZipArchive(path: String, target: String): Boolean = archiveOperation {
        RustGraphqlApi.mutate("extractZipArchive(path: ${gql(path)}, target: ${gql(target)})", longRunning = true).getValue("extractZipArchive").jsonPrimitive.content
    }

    suspend fun cacheZipEntry(path: String): String? = try {
        RustGraphqlApi.mutate("cacheZipEntry(path: ${gql(path)})").getValue("cacheZipEntry").jsonPrimitive.content
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
        RustGraphqlApi.query("indexedFiles(query: ${gql(query)}, limit: $limit, offset: $offset, sortBy: ${sortBy.name}) { $fields }").getValue("indexedFiles").jsonArray.map { decode(it.jsonObject) }
    suspend fun recent(): List<FileEntry> =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(
            RustGraphqlApi.query("recentFiles { $fields }").getValue("recentFiles")
        )

    suspend fun indexedCount(query: String): Int = RustGraphqlApi.query("indexedFileCount(query: ${gql(query)})").getValue("indexedFileCount").jsonPrimitive.int

    private suspend fun archiveOperation(operation: suspend () -> String): Boolean = try {
        operation()
        true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }

    private const val fields = "name path permission createdAt updatedAt size isDir childCount mediaId"
    suspend fun rename(path: String, name: String): String? {
        val value = RustGraphqlApi.mutate("renameFile(path: ${gql(path)}, name: ${gql(name)})").getValue("renameFile")
        return if (value is JsonNull) null else value.jsonPrimitive.content
    }
    suspend fun delete(path: String): Boolean = deleteMany(listOf(path)) > 0

    suspend fun deleteMany(paths: List<String>): Int = RustGraphqlApi.mutate(
        "deleteFiles(paths: ${com.ismartcoding.plain.rust.graphql.gqlIds(paths)}) { affectedCount }",
        longRunning = true,
    ).getValue("deleteFiles").jsonObject.getValue("affectedCount").jsonPrimitive.int

    private suspend fun read(root: String, query: String, sortBy: FileSortBy, offset: Int = 0, limit: Int? = null): List<FileEntry> =
        RustGraphqlApi.query("files(root: ${gql(root)}, query: ${gql(query)}, sortBy: ${sortBy.name}, offset: $offset, limit: ${limit ?: Int.MAX_VALUE}) { $fields }")
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
        val operation = when (action) {
            "createDirectory" -> "createDir(path: ${gql(target)})"
            "createFile" -> "createFile(path: ${gql(target)})"
            "writeText" -> "writeTextFile(path: ${gql(target)}, content: ${gql(content)}, overwrite: $overwrite)"
            else -> error("Unknown file write action")
        }
        return decode(RustGraphqlApi.mutate("$operation { $fields }").getValue(operation.substringBefore('(')).jsonObject)
    }

    private fun decode(row: JsonObject): FileEntry = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(row)
}
