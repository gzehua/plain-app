package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.models.ContactGroup
import com.ismartcoding.plain.appContext
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.provider.ContactsContract
import com.ismartcoding.plain.lib.extensions.forEach
import com.ismartcoding.plain.lib.extensions.getLongValue
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.queryCursor
import com.ismartcoding.plain.contentResolver
import java.util.ArrayList
import android.content.Context
import com.ismartcoding.plain.rust.host.provider.ContactGroupFacts

internal object GroupHelper {
    internal fun getAll(context: Context = appContext): List<ContactGroupFacts> {
        val groups = ArrayList<ContactGroupFacts>()
        val uri = ContactsContract.Groups.CONTENT_URI
        val projection =
            arrayOf(
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE,
                ContactsContract.Groups.SYSTEM_ID,
                ContactsContract.Groups.AUTO_ADD,
                ContactsContract.Groups.FAVORITES,
            )

        context.contentResolver.queryCursor(uri, projection)?.forEach { cursor, cache ->
            val id = cursor.getLongValue(ContactsContract.Groups._ID, cache)
            val title = cursor.getStringValue(ContactsContract.Groups.TITLE, cache)

            val systemId = cursor.getStringValue(ContactsContract.Groups.SYSTEM_ID, cache)
            groups.add(ContactGroupFacts(
                id.toString(), title, systemId,
                cursor.getStringValue(ContactsContract.Groups.AUTO_ADD, cache) != "0",
                cursor.getStringValue(ContactsContract.Groups.FAVORITES, cache) != "0",
            ))
        }
        return groups
    }

    fun create(
        name: String,
        accountName: String,
        accountType: String,
    ): ContactGroup {
        val operations = ArrayList<ContentProviderOperation>()
        ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI).apply {
            withValue(ContactsContract.Groups.TITLE, name)
            withValue(ContactsContract.Groups.GROUP_VISIBLE, 1)
            withValue(ContactsContract.Groups.ACCOUNT_NAME, accountName)
            withValue(ContactsContract.Groups.ACCOUNT_TYPE, accountType)
            operations.add(build())
        }

        val results = contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        val rawId = ContentUris.parseId(results[0].uri!!)
        return ContactGroup(rawId.toString(), name)
    }

    fun update(
        id: String,
        name: String,
    ) {
        val context = appContext
        val operations = ArrayList<ContentProviderOperation>()
        ContentProviderOperation.newUpdate(ContactsContract.Groups.CONTENT_URI).apply {
            val selection = "${ContactsContract.Groups._ID} = ?"
            val selectionArgs = arrayOf(id)
            withSelection(selection, selectionArgs)
            withValue(ContactsContract.Groups.TITLE, name)
            operations.add(build())
        }
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    }

    fun delete(id: String) {
        val context = appContext
        val operations = ArrayList<ContentProviderOperation>()
        val uri =
            ContentUris.withAppendedId(ContactsContract.Groups.CONTENT_URI, id.toLong()).buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build()

        operations.add(ContentProviderOperation.newDelete(uri).build())
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    }
}
