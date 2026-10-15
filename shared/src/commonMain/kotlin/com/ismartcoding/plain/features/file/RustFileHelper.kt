package com.ismartcoding.plain.features.file

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import io.ktor.http.Url
import io.ktor.http.decodeURLPart
import kotlinx.serialization.json.*
import kotlin.time.Instant

object RustFileHelper {
    suspend fun createZipArchive(paths: List<String>, target: String): String =
        PlainRust.mainMutate("createZipArchive(paths: [${paths.joinToString(",") { gql(it) }}], target: ${gql(target)})", longRunning = true).getValue("createZipArchive").jsonPrimitive.content
    suspend fun extractZipArchive(path: String, target: String): String =
        PlainRust.mainMutate("extractZipArchive(path: ${gql(path)}, target: ${gql(target)})", longRunning = true).getValue("extractZipArchive").jsonPrimitive.content

    suspend fun cacheZipEntry(path: String): String = PlainRust.mainMutate("cacheZipEntry(path: ${gql(path)})").getValue("cacheZipEntry").jsonPrimitive.content

    suspend fun createDirectory(path: String): DFile = write("createDirectory", path)
    suspend fun createFile(path: String): DFile = write("createFile", path)
    suspend fun writeText(path: String, content: String, overwrite: Boolean): DFile = write("writeText", path, content, overwrite)

    suspend fun list(root: String, showHidden: Boolean, sortBy: FileSortBy): List<DFile> = read(root, "", null, showHidden, sortBy)["items"]!!.jsonArray.map { decode(it.jsonObject) }
    suspend fun searchName(text: String, root: String, showHidden: Boolean, sortBy: FileSortBy): List<DFile> = read(root, "", text, showHidden, sortBy)["items"]!!.jsonArray.map { decode(it.jsonObject) }
    suspend fun search(query: String, root: String, sortBy: FileSortBy, offset: Int = 0, limit: Int? = null): List<DFile> = read(root, query, null, null, sortBy, offset, limit)["items"]!!.jsonArray.map { decode(it.jsonObject) }
    suspend fun count(query: String, root: String): Int = read(root, query, null, null, FileSortBy.NAME_ASC, countOnly = true).getValue("count").jsonPrimitive.int

    suspend fun indexed(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<DFile> =
        PlainRust.mainQuery("indexedFiles(query: ${gql(query)}, limit: $limit, offset: $offset, sortBy: ${sortBy.name}) { $fields }").getValue("indexedFiles").jsonArray.map { decode(it.jsonObject) }
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

    private suspend fun write(action: String, path: String, content: String = "", overwrite: Boolean = false): DFile {
        val target = if (path.startsWith("file://")) Url(path).encodedPath.decodeURLPart() else path
        val operation = when (action) {
            "createDirectory" -> "createDir(path: ${gql(target)})"
            "createFile" -> "createFile(path: ${gql(target)})"
            "writeText" -> "writeTextFile(path: ${gql(target)}, content: ${gql(content)}, overwrite: $overwrite)"
            else -> error("Unknown file write action")
        }
        return decode(PlainRust.mainMutate("$operation { $fields }").getValue(operation.substringBefore('(')).jsonObject)
    }

    private fun decode(row: JsonObject): DFile = DFile(
            name = row.getValue("name").jsonPrimitive.content,
            path = row.getValue("path").jsonPrimitive.content,
            permission = row.getValue("permission").jsonPrimitive.content,
            createdAt = row["createdAt"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content?.let(Instant::parse),
            updatedAt = Instant.parse(row.getValue("updatedAt").jsonPrimitive.content),
            size = row.getValue("size").jsonPrimitive.long,
            isDir = row.getValue("isDir").jsonPrimitive.boolean,
            childCount = row.getValue("childCount").jsonPrimitive.int,
            mediaId = row["mediaId"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content.orEmpty(),
    )
}
