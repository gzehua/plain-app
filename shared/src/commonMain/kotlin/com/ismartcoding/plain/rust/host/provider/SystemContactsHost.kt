package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.features.contacts.ContactInput

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemContactsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemContactFacts" -> {
            val request = JsonHelper.jsonDecodeFromElement<ProviderPageRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.readContacts(request.plan, request.limit, request.offset))
        }
        "systemContactCount" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.media.countMedia(
                com.ismartcoding.plain.platform.system.DataType.CONTACT,
                params.providerPlan(),
            )
        )
        "systemContactIds" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.media.getMediaIds(
                com.ismartcoding.plain.platform.system.DataType.CONTACT,
                params.providerPlan(),
            )
        )
        "systemContactGroupFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.getContactGroups())
        "systemContactSources" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.getContactSources())
        "systemCreateContact" -> {
            val request = JsonHelper.jsonDecodeFromElement<CreateContactRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.createContact(request.input, request.accountType))
        }
        "systemUpdateContact" -> {
            val request = JsonHelper.jsonDecodeFromElement<UpdateContactRequest>(params)
            com.ismartcoding.plain.platform.media.updateContact(
                request.id, request.input,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemCreateContactGroup" -> {
            val request = JsonHelper.jsonDecodeFromElement<CreateContactGroupRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.createContactGroup(
                request.name, request.accountName, request.accountType,
            ).id.toString())
        }
        "systemUpdateContactGroup" -> {
            val request = JsonHelper.jsonDecodeFromElement<UpdateContactGroupRequest>(params)
            com.ismartcoding.plain.platform.contacts.updateContactGroup(
                request.id, request.name,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteContactGroup" -> {
            com.ismartcoding.plain.platform.contacts.deleteContactGroup(JsonHelper.jsonDecodeFromElement<IdRequest>(params).id)
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
