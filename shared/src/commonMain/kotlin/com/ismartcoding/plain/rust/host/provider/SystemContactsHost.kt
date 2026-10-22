package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.features.contacts.ContactInput
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.contacts.createContactGroup
import com.ismartcoding.plain.platform.contacts.deleteContactGroup
import com.ismartcoding.plain.platform.contacts.getContactGroups
import com.ismartcoding.plain.platform.contacts.getContactSources
import com.ismartcoding.plain.platform.contacts.readContacts
import com.ismartcoding.plain.platform.contacts.updateContactGroup
import com.ismartcoding.plain.platform.media.countMedia
import com.ismartcoding.plain.platform.media.createContact
import com.ismartcoding.plain.platform.media.getMediaIds
import com.ismartcoding.plain.platform.media.updateContact
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object SystemContactsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemContactFacts" -> {
            val request = JsonHelper.jsonDecodeFromElement<ProviderPageRequest>(params)
            JsonHelper.jsonEncodeToElement(readContacts(request.plan, request.limit, request.offset))
        }
        "systemContactCount" -> JsonHelper.jsonEncodeToElement(
            countMedia(
                DataType.CONTACT,
                params.providerPlan(),
            )
        )
        "systemContactIds" -> JsonHelper.jsonEncodeToElement(
            getMediaIds(
                DataType.CONTACT,
                params.providerPlan(),
            )
        )
        "systemContactGroupFacts" -> JsonHelper.jsonEncodeToElement(getContactGroups())
        "systemContactSources" -> JsonHelper.jsonEncodeToElement(getContactSources())
        "systemCreateContact" -> {
            val request = JsonHelper.jsonDecodeFromElement<CreateContactRequest>(params)
            JsonHelper.jsonEncodeToElement(createContact(request.input, request.accountType))
        }
        "systemUpdateContact" -> {
            val request = JsonHelper.jsonDecodeFromElement<UpdateContactRequest>(params)
            updateContact(
                request.id, request.input,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemCreateContactGroup" -> {
            val request = JsonHelper.jsonDecodeFromElement<CreateContactGroupRequest>(params)
            JsonHelper.jsonEncodeToElement(createContactGroup(
                request.name, request.accountName, request.accountType,
            ).id.toString())
        }
        "systemUpdateContactGroup" -> {
            val request = JsonHelper.jsonDecodeFromElement<UpdateContactGroupRequest>(params)
            updateContactGroup(
                request.id, request.name,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteContactGroup" -> {
            deleteContactGroup(JsonHelper.jsonDecodeFromElement<IdRequest>(params).id)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }

}

@Serializable
private data class CreateContactRequest(val input: ContactInput, val accountType: String)

@Serializable
private data class UpdateContactRequest(val id: String, val input: ContactInput)

@Serializable
private data class CreateContactGroupRequest(val name: String, val accountName: String, val accountType: String)

@Serializable
private data class UpdateContactGroupRequest(val id: String, val name: String)
