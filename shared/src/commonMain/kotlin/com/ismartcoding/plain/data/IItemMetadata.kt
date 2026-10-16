package com.ismartcoding.plain.data

import com.ismartcoding.plain.graphql.models.Audio as AudioTrack

/**
 * Optional metadata for [com.ismartcoding.plain.db.IData] items that have a human-readable title and a byte size.
 * `create()` (in `TagRelationStub.Companion`) pattern-matches on this so the same
 * shared code can populate [TagRelationStub.title]/[TagRelationStub.size] for any
 * implementation — including `AudioTrack`, which lives in `app/` (Android-only) and
 * implements this interface declared in `shared/`.
 */
interface IItemMetadata {
    val title: String
    val size: Long
}
