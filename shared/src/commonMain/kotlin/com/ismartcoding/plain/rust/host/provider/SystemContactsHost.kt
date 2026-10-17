package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemContactsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemContactFacts" -> {
            val plan = params.providerPlan()
            val offset = params.getValue("offset").jsonPrimitive.int
            val limit = params.getValue("limit").jsonPrimitive.int
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.readContacts(plan, limit, offset))
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
        "systemCreateContact" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.createContact(
            contactInput(params), params.getValue("accountType").jsonPrimitive.content))
        "systemUpdateContact" -> {
            com.ismartcoding.plain.platform.media.updateContact(
                params.getValue("id").jsonPrimitive.content,
                contactInput(params),
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemCreateContactGroup" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.contacts.createContactGroup(
            params.getValue("name").jsonPrimitive.content,
            params.getValue("accountName").jsonPrimitive.content,
            params.getValue("accountType").jsonPrimitive.content,
            ).id.toString())
        "systemUpdateContactGroup" -> {
            com.ismartcoding.plain.platform.contacts.updateContactGroup(
                params.getValue("id").jsonPrimitive.content,
                params.getValue("name").jsonPrimitive.content,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteContactGroup" -> {
            com.ismartcoding.plain.platform.contacts.deleteContactGroup(params.getValue("id").jsonPrimitive.content)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }

    private fun contactInput(params: JsonObject): com.ismartcoding.plain.features.contacts.ContactInput =
        JsonHelper.jsonDecodeFromElement(params.getValue("input"))

}
