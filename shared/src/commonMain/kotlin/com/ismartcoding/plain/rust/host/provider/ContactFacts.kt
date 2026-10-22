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

@Serializable
internal data class ContactDataFacts(
    val rawId: String,
    val mimeType: String,
    val data1: String,
    val data2: String,
    val data3: String,
    val data4: String,
    val data5: String,
    val data6: String,
)

@Serializable
internal data class ContactGroupFacts(
    val id: String,
    val name: String,
    val systemId: String = "",
    val autoAdd: Boolean = false,
    val favorites: Boolean = false,
)

@Serializable
internal data class ContactPageFacts(
    val rows: List<ContactFacts> = emptyList(),
    val details: List<ContactDataFacts> = emptyList(),
    val groups: List<ContactGroupFacts> = emptyList(),
)

@Serializable
internal data class ContactSourceFacts(
    val name: String,
    val type: String,
)
