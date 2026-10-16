package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.helpers.FilePathValidator
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.features.chat.*
import com.ismartcoding.plain.ui.chat.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.images.search.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.shares.*
import com.ismartcoding.plain.ui.shares.*
import com.ismartcoding.plain.platform.*
import com.ismartcoding.plain.platform.contacts.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.network.*
import com.ismartcoding.plain.platform.permissions.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.ui.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.lib.withIO
import kotlinx.serialization.json.*

object FileTaskHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = withIO {
        val paths = params.getValue("paths").jsonArray.map { it.jsonPrimitive.content }
        when (method) {
            "fileTaskAuthorize" -> {
                val canonical = paths.map(::getCanonicalPath)
                FilePathValidator.requireAllSafe(canonical.filterNot(::isPrivate))
                if (canonical.any { !isPrivate(it) }) check(Permission.WRITE_EXTERNAL_STORAGE.isGranted()) { "File access permission denied" }
            }
            "fileTaskScan" -> scanFileTaskPaths(paths.filterNot { isPrivate(getCanonicalPath(it)) })
            "fileTaskMediaSnapshot" -> {
                require(paths.size <= 128)
                val external = paths.filterNot { isPrivate(getCanonicalPath(it)) }
                FilePathValidator.requireAllSafe(external.map(::getCanonicalPath))
                return@withIO buildJsonArray {
                    queryFileTaskMedia(external).forEach { row -> add(buildJsonObject {
                        put("mediaType", row.type.value); put("mediaId", row.id); put("path", row.path)
                    }) }
                }
            }
            else -> error("Unknown file host method")
        }
        JsonPrimitive(true)
    }
    private fun isPrivate(path: String): Boolean = listOf(appDir(),cacheDirPath()).filter { it.isNotEmpty() }.map(::getCanonicalPath).any { path == it || path.startsWith("$it/") }
}
