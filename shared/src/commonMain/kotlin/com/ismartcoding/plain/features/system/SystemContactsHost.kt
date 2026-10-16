package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.features.contact.DContentItem

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemContactsHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemContactFacts" -> {
            val plan = params.providerPlan()
            val offset = params.getValue("offset").jsonPrimitive.int
            val limit = params.getValue("limit").jsonPrimitive.int
            JsonHelper.jsonEncodeToElement(
                com.ismartcoding.plain.platform.searchMedia(
                    com.ismartcoding.plain.enums.DataType.CONTACT, plan, limit, offset,
                    com.ismartcoding.plain.features.file.FileSortBy.DATE_DESC,
                ).filterIsInstance<ContactFacts>()
            )
        }
        "systemContactCount" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.countMedia(
                com.ismartcoding.plain.enums.DataType.CONTACT,
                params.providerPlan(),
            )
        )
        "systemContactIds" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getMediaIds(
                com.ismartcoding.plain.enums.DataType.CONTACT,
                params.providerPlan(),
            )
        )
        "systemContactGroupFacts" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getContactGroups().map { group ->
                ContactGroupFacts(
                    id = group.id.toString(),
                    name = group.name,
                )
            }
        )
        "systemContactSources" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getContactSources().map { source ->
                ContactSourceFacts(
                    name = source.name,
                    type = source.type,
                )
            }
        )
        "systemCreateContact" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.createContact(
            contactInput(params)))
        "systemUpdateContact" -> {
            com.ismartcoding.plain.platform.updateContact(
                params.getValue("id").jsonPrimitive.content,
                contactInput(params),
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemCreateContactGroup" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.createContactGroup(
            params.getValue("name").jsonPrimitive.content,
            params.getValue("accountName").jsonPrimitive.content,
            params.getValue("accountType").jsonPrimitive.content,
            ).id.toString())
        "systemUpdateContactGroup" -> {
            com.ismartcoding.plain.platform.updateContactGroup(
                params.getValue("id").jsonPrimitive.content,
                params.getValue("name").jsonPrimitive.content,
            )
            JsonHelper.jsonEncodeToElement(true)
        }
        "systemDeleteContactGroup" -> {
            com.ismartcoding.plain.platform.deleteContactGroup(params.getValue("id").jsonPrimitive.content)
            JsonHelper.jsonEncodeToElement(true)
        }
        else -> error("Unsupported provider operation")
    }

    private fun contactInput(params: JsonObject): com.ismartcoding.plain.features.contact.ContactInput =
        JsonHelper.jsonDecodeFromElement(params.getValue("input"))

}
