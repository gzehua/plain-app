package com.ismartcoding.plain.ui.media.thumbnail

import kotlinx.serialization.json.*
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.request.ContentApiSession
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
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.rust.http.*

object ThumbnailHost {
    suspend fun handle(session: ContentApiSession, method: String, params: JsonObject): JsonElement {
        if (method == "thumbnailAuthorize") {
            check(readFileRange(params.getValue("paths").jsonArray.single().jsonPrimitive.content, 0, 1) != null) { "Thumbnail source access denied" }
            return JsonPrimitive(true)
        }
        check(method == "thumbnailDecode")
        val path = params.getValue("path").jsonPrimitive.content
        check(readFileRange(path, 0, 1) != null) { "Thumbnail source access denied" }
        val bytes = decodeThumbnailBytes(path,
            params.getValue("width").jsonPrimitive.int,
            params.getValue("height").jsonPrimitive.int,
            params.getValue("centerCrop").jsonPrimitive.boolean,
            params.getValue("mediaId").jsonPrimitive.content,
            params.getValue("fileName").jsonPrimitive.content,
        ) ?: return JsonNull
        val token = params.getValue("outputToken").jsonPrimitive.content
        val error = RustCoreBridge.send("thumbnail", token, bytes)
        check(error.isEmpty()) { error }
        return JsonPrimitive(true)
    }
}
