package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.api.graphql.models.ContactSource
import com.ismartcoding.plain.appContext
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.ismartcoding.plain.lib.extensions.forEach
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.queryCursor
object SourceHelper {
    fun getAll(): List<ContactSource> {
        val context = appContext
        val sources = mutableListOf<ContactSource>()
        setOf(
            ContactsContract.Groups.CONTENT_URI,
            ContactsContract.Settings.CONTENT_URI,
            ContactsContract.RawContacts.CONTENT_URI,
        ).forEach {
            fillSourcesFromUri(context, it, sources)
        }

        return sources
    }

    private fun fillSourcesFromUri(
        context: Context,
        uri: Uri,
        sources: MutableList<ContactSource>,
    ) {
        context.contentResolver.queryCursor(
            uri,
            arrayOf(
                ContactsContract.RawContacts.ACCOUNT_NAME,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
            ),
        )?.forEach { cursor, cache ->
            val name = cursor.getStringValue(ContactsContract.RawContacts.ACCOUNT_NAME, cache)
            val type = cursor.getStringValue(ContactsContract.RawContacts.ACCOUNT_TYPE, cache)
            if (!sources.any { it.name == name && it.type == type }) {
                sources.add(ContactSource(name, type))
            }
        }
    }
}
