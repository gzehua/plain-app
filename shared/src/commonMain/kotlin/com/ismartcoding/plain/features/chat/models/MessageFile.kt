package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.IData
import com.ismartcoding.plain.lib.generateId
import kotlinx.serialization.Serializable

@Serializable(with = MessageFileSerializer::class)
data class MessageFile(
    override var id: String = generateId(),
    val uri: String,
    val size: Long,
    val durationMs: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val summary: String = "",
    val fileName: String = "",
) : IData {
    /** True when this file must be downloaded from a remote peer (fsid: scheme). */
    fun isRemoteFile(): Boolean = uri.startsWith("fsid:")

    /**
     * True when this file is stored in the local content-addressable store.
     * The [uri] has the form  fid:{sha256hex}.
     */
    fun isFidFile(): Boolean = uri.startsWith("fid:")

    /**
     * Returns the SHA-256 fileId for fid: URIs.
     * Returns an empty string for other URI schemes.
     */
    fun localFileId(): String = if (isFidFile()) uri.removePrefix("fid:") else ""

    /** Remote fileId extracted from a fsid: URI (used as query param for /fs endpoint). */
    fun parseFileId(): String = uri.replace("fsid:", "")
}
