package com.ismartcoding.plain.features.media

import com.ismartcoding.plain.platform.system.DataType

/** Media-library item kinds — the narrowed type accepted by media-only
 *  operations (trash/restore/delete/move, buckets), unlike the catch-all
 *  [DataType] which also spans SMS/contacts/etc. */
enum class MediaDataType {
    AUDIO,
    VIDEO,
    IMAGE,
    DOC,
}

fun MediaDataType.toDataType(): DataType = DataType.valueOf(name)
