package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.helpers.FilterField
import kotlin.time.Instant
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import android.content.Context
import android.provider.BaseColumns
import android.provider.Telephony
import androidx.core.net.toUri
import com.ismartcoding.plain.helpers.ContentWhere
import com.ismartcoding.plain.lib.extensions.find
import com.ismartcoding.plain.lib.extensions.getIntValue
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.getTimeSecondsValue
import com.ismartcoding.plain.lib.extensions.getTimeValue
import com.ismartcoding.plain.lib.extensions.map
import com.ismartcoding.plain.lib.extensions.queryCursor
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.telephony.getSims
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object SmsConversationHelper {
    suspend fun facts(context: Context, params: JsonObject): JsonElement = withIO {
        val request = JsonHelper.jsonDecodeFromElement<SmsConversationsRequest>(params)
        val ids = request.ids
        val beforeDates = request.beforeDates
        val items=mutableListOf<SmsConversationRowFacts>()
        val recipientIds=mutableMapOf<String,List<String>>()
        (ids?.chunked(400) ?: listOf(emptyList())).forEach { batch ->
            val where=ContentWhere()
            if (ids!=null) where.addIn(BaseColumns._ID,batch)
            context.contentResolver.queryCursor(conversationsUri,
                arrayOf(BaseColumns._ID,Telephony.Threads.SNIPPET,Telephony.Threads.DATE,Telephony.Threads.MESSAGE_COUNT,Telephony.Threads.READ,"recipient_ids"),
                if(ids==null)null else where.toSelection(),if(ids==null)null else where.args.toTypedArray(),"${Telephony.Threads.DATE} DESC")?.use { cursor ->
                val cache=mutableMapOf<String,Int>()
                while(cursor.moveToNext()) {
                    val id=cursor.getStringValue(BaseColumns._ID,cache)
                    items.add(SmsConversationRowFacts(id,"",cursor.getStringValue(Telephony.Threads.SNIPPET,cache),cursor.getTimeValue(Telephony.Threads.DATE,cache),cursor.getIntValue(Telephony.Threads.MESSAGE_COUNT,cache),cursor.getIntValue(Telephony.Threads.READ,cache)==1))
                    val raw=cursor.getStringValue("recipient_ids",cache)
                    if(raw.isNotEmpty())recipientIds[id]=SmsProviderContract.parseRecipientIds(raw)
                }
            }
        }
        val addressMap=batchGetCanonicalAddresses(context,recipientIds.values.flatten().toSet())
        val own=getSims().map{it.number}.filter{it.isNotEmpty()}
        val rows=items.map{item->
            val resolved=recipientIds[item.id].orEmpty().mapNotNull(addressMap::get)
            item.copy(address=resolved.firstOrNull().orEmpty(),addresses=resolved)
        }
        JsonHelper.jsonEncodeToElement(SmsConversationFacts(
            items = rows,
            ownNumbers = own,
            snippetCandidates = beforeDates.mapValues { (id, value) ->
                getSnippetsBeforeDate(context, id, value)
            },
        ))
    }

    private val conversationsUri = "content://mms-sms/conversations?simple=true".toUri()
    private val smsUri = Telephony.Sms.CONTENT_URI
    private val mmsUri = Telephony.Mms.CONTENT_URI

    private fun getSnippetsBeforeDate(context: Context, threadId: String, beforeDate: Long): List<SmsSnippetFacts> {
        val smsSnippet = context.contentResolver.queryCursor(
            smsUri,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE),
            "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.DATE} <= ?",
            arrayOf(threadId, beforeDate.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT 1",
        )?.find { cursor, cache ->
            SmsSnippetFacts(
                value = cursor.getStringValue(Telephony.Sms.BODY, cache),
                dateMillis = cursor.getTimeValue(Telephony.Sms.DATE, cache).toEpochMilliseconds(),
            )
        }
        val mmsSnippet = context.contentResolver.queryCursor(
            mmsUri,
            arrayOf(Telephony.Mms._ID, Telephony.Mms.DATE),
            "${Telephony.Mms.THREAD_ID} = ? AND ${Telephony.Mms.DATE} <= ? AND ${SmsProviderContract.MMS_CONTENT_FILTER}",
            arrayOf(threadId, (beforeDate / 1000).toString()),
            "${Telephony.Mms.DATE} DESC LIMIT 1",
        )?.find { cursor, cache ->
            val id = cursor.getStringValue(Telephony.Mms._ID, cache)
            SmsSnippetFacts(
                value = "",
                dateMillis = cursor.getTimeSecondsValue(Telephony.Mms.DATE, cache).toEpochMilliseconds(),
                parts = SmsHelper.readMmsParts(context, id),
            )
        }
        return listOfNotNull(smsSnippet, mmsSnippet)
    }

    private val canonicalAddressesUri = "content://mms-sms/canonical-addresses".toUri()

    private fun batchGetCanonicalAddresses(context: Context, recipientIds: Set<String>): Map<String, String> {
        if (recipientIds.isEmpty()) return emptyMap()

        val predicate = SmsProviderContract.numericIdPredicate(BaseColumns._ID, recipientIds) ?: return emptyMap()
        val result = mutableMapOf<String, String>()

        context.contentResolver.queryCursor(
            canonicalAddressesUri,
            arrayOf(BaseColumns._ID, "address"),
            predicate,
            null,
            null
        )?.use { cursor ->
            val cache = mutableMapOf<String, Int>()
            while (cursor.moveToNext()) {
                val id = cursor.getStringValue(BaseColumns._ID, cache)
                val address = cursor.getStringValue("address", cache)
                if (address.isNotEmpty()) result[id] = address
            }
        }

        return result
    }

}

@Serializable
private data class SmsConversationsRequest(val ids: List<String>? = null, val beforeDates: Map<String, Long> = emptyMap())
