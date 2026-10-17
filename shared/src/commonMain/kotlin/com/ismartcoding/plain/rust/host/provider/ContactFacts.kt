package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ContactFacts(
    val id: String,
    val prefix: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val suffix: String,
    val photoId: String,
    val source: String,
    val starred: Boolean,
    val contactId: String,
    val thumbnailId: String,
    val ringtone: String,
    val updatedAt: String,
)
