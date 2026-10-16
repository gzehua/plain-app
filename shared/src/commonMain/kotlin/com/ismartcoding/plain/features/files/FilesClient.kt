package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import io.ktor.http.Url
import io.ktor.http.decodeURLPart
import kotlinx.serialization.json.*

object FilesClient {
    suspend fun createZipArchive(paths: List<String>, target: String): String =
        PlainRust.mainMutate("createZipArchive(paths: [${paths.joinToString(",") { gql(it) }}], target: ${gql(target)})", longRunning = true).getValue("createZipArchive").jsonPrimitive.content
    suspend fun extractZipArchive(path: String, target: String): String =
        PlainRust.mainMutate("extractZipArchive(path: ${gql(path)}, target: ${gql(target)})", longRunning = true).getValue("extractZipArchive").jsonPrimitive.content

    suspend fun cacheZipEntry(path: String): String = PlainRust.mainMutate("cacheZipEntry(path: ${gql(path)})").getValue("cacheZipEntry").jsonPrimitive.content

    suspend fun createDirectory(path: String): FileEntry = write("createDirectory", path)
    suspend fun createFile(path: String): FileEntry = write("createFile", path)
    suspend fun writeText(path: String, content: String, overwrite: Boolean): FileEntry = write("writeText", path, content, overwrite)

    suspend fun list(root: String, showHidden: Boolean, sortBy: FileSortBy): List<FileEntry> = read(root, "", null, showHidden, sortBy)["items"]!!.jsonArray.map { decode(it.jsonObject) }
    suspend fun searchName(text: String, root: String, showHidden: Boolean, sortBy: FileSortBy): List<FileEntry> = read(root, "", text, showHidden, sortBy)["items"]!!.jsonArray.map { decode(it.jsonObject) }
    suspend fun search(query: String, root: String, sortBy: FileSortBy, offset: Int = 0, limit: Int? = null): List<FileEntry> = read(root, query, null, null, sortBy, offset, limit)["items"]!!.jsonArray.map { decode(it.jsonObject) }
    suspend fun count(query: String, root: String): Int = read(root, query, null, null, FileSortBy.NAME_ASC, countOnly = true).getValue("count").jsonPrimitive.int

    suspend fun indexed(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<FileEntry> =
        PlainRust.mainQuery("indexedFiles(query: ${gql(query)}, limit: $limit, offset: $offset, sortBy: ${sortBy.name}) { $fields }").getValue("indexedFiles").jsonArray.map { decode(it.jsonObject) }
    suspend fun recent(): List<FileEntry> =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(
            PlainRust.mainQuery("recentFiles { $fields }").getValue("recentFiles")
        )

    suspend fun indexedCount(query: String): Int = PlainRust.mainQuery("indexedFileCount(query: ${gql(query)})").getValue("indexedFileCount").jsonPrimitive.int

    private const val fields = "name path permission createdAt updatedAt size isDir childCount mediaId"
    suspend fun rename(path: String, name: String): String? {
        val value = PlainRust.mainMutate("renameFilePath(path: ${gql(path)}, name: ${gql(name)})").getValue("renameFilePath")
        return if (value is JsonNull) null else value.jsonPrimitive.content
    }
    suspend fun delete(path: String): Boolean = PlainRust.mainMutate("deleteFiles(paths: [${gql(path)}]) { affectedCount }")
        .getValue("deleteFiles").jsonObject.getValue("affectedCount").jsonPrimitive.int > 0

    private suspend fun read(root: String, query: String, text: String?, showHidden: Boolean?, sortBy: FileSortBy, offset: Int = 0, limit: Int? = null, countOnly: Boolean = false): JsonObject =
        PlainRust.mainQuery("browseFiles(root: ${gql(root)}, query: ${gql(query)}, text: ${text?.let(::gql) ?: "null"}, showHidden: ${showHidden ?: "null"}, sortBy: ${sortBy.name}, offset: $offset, limit: ${limit ?: Int.MAX_VALUE}, countOnly: $countOnly) { count items { $fields } }").getValue("browseFiles").jsonObject

    private suspend fun write(action: String, path: String, content: String = "", overwrite: Boolean = false): FileEntry {
        val target = if (path.startsWith("file://")) Url(path).encodedPath.decodeURLPart() else path
        val operation = when (action) {
            "createDirectory" -> "createDir(path: ${gql(target)})"
            "createFile" -> "createFile(path: ${gql(target)})"
            "writeText" -> "writeTextFile(path: ${gql(target)}, content: ${gql(content)}, overwrite: $overwrite)"
            else -> error("Unknown file write action")
        }
        return decode(PlainRust.mainMutate("$operation { $fields }").getValue(operation.substringBefore('(')).jsonObject)
    }

    private fun decode(row: JsonObject): FileEntry = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(row)
}
