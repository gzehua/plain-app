package com.ismartcoding.plain.features.telephony.sms

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.net.toUri
import com.ismartcoding.plain.helpers.ContentWhere
import com.ismartcoding.plain.lib.extensions.find
import com.ismartcoding.plain.lib.extensions.getIntValue
import com.ismartcoding.plain.lib.extensions.getStringValue
import com.ismartcoding.plain.lib.extensions.getTimeSecondsValue
import com.ismartcoding.plain.lib.extensions.getTimeValue
import com.ismartcoding.plain.lib.extensions.map
import com.ismartcoding.plain.lib.extensions.queryCursor
import com.ismartcoding.plain.platform.telephony.getSims
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import com.ismartcoding.plain.smsManager
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.receivers.SmsSentReceiver

object SmsHelper {
    // Lazy so merely touching this object (e.g. from the server stop hooks or
    // JVM host tests) never initializes the Telephony provider classes.
    private val smsUri by lazy { Telephony.Sms.CONTENT_URI }
    private val mmsUri by lazy { Telephony.Mms.CONTENT_URI }
    private val mmsPartUri by lazy { "content://mms/part".toUri() }

    private const val MMS_ADDR_TYPE_FROM = 137
    private const val MMS_ADDR_TYPE_TO = 151

    private fun manager(subscriptionId: Int?): SmsManager =
        if (subscriptionId != null && subscriptionId >= 0) {
            @Suppress("DEPRECATION")
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        } else smsManager

    fun divideText(body: String, subscriptionId: Int?): List<String> = manager(subscriptionId).divideMessage(body)

    fun sendParts(request: SmsDispatchRequest) {
        val manager = manager(request.subscriptionId)
        val parts = request.parts
        require(parts.isNotEmpty())
        val sentIntents = ArrayList(parts.indices.map { partIndex ->
            val identity = SmsProviderContract.smsSentIntentIdentity(appContext.packageName, request.requestId, partIndex)
            val intent = Intent(appContext, SmsSentReceiver::class.java).apply {
                action = identity.action
                data = Uri.parse(identity.data)
                putExtra(SmsSentReceiver.EXTRA_REQUEST_ID, request.requestId)
                putExtra(SmsSentReceiver.EXTRA_PART_INDEX, partIndex)
                putExtra(SmsSentReceiver.EXTRA_PART_COUNT, parts.size)
            }
            PendingIntent.getBroadcast(appContext, (request.requestId.hashCode() * 31) + partIndex, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        })
        if (parts.size > 1) manager.sendMultipartTextMessage(request.number, null, ArrayList(parts), sentIntents, null)
        else manager.sendTextMessage(request.number, null, parts.single(), sentIntents.single(), null)
    }

    private fun getProjection(): Array<String> {
        return arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.TYPE,
            Telephony.Sms.BODY,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.READ,
            Telephony.Sms.DATE,
            Telephony.Sms.SERVICE_CENTER,
            Telephony.Sms.SUBSCRIPTION_ID,
        )
    }

    private fun cursorToSmsMessage(cursor: Cursor, cache: MutableMap<String, Int>): SmsRowFacts {
        return SmsRowFacts(
            cursor.getStringValue(Telephony.Sms._ID, cache),
            cursor.getStringValue(Telephony.Sms.BODY, cache),
            cursor.getStringValue(Telephony.Sms.ADDRESS, cache),
            cursor.getTimeValue(Telephony.Sms.DATE, cache),
            cursor.getStringValue(Telephony.Sms.SERVICE_CENTER, cache),
            cursor.getIntValue(Telephony.Sms.READ, cache) == 1,
            cursor.getStringValue(Telephony.Sms.THREAD_ID, cache),
            cursor.getIntValue(Telephony.Sms.TYPE, cache),
            cursor.getIntValue(Telephony.Sms.SUBSCRIPTION_ID, cache, -1),
        )
    }

    private fun queryCount(context: Context, uri: Uri, selection: String? = null, selectionArgs: Array<String>? = null): Int {
        var count = 0
        context.contentResolver.queryCursor(
            uri, arrayOf("COUNT(*) as count"),
            selection, selectionArgs, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) count = cursor.getInt(0)
        }
        return count
    }

    private fun cursorToMmsMessage(context: Context, cursor: Cursor, cache: MutableMap<String, Int>): SmsRowFacts {
        val rawMmsId = cursor.getStringValue(Telephony.Mms._ID, cache)
        return SmsRowFacts(
            id = "mms_$rawMmsId",
            body = "",
            address = "",
            addressCandidates = readMmsAddresses(context, rawMmsId),
            date = cursor.getTimeSecondsValue(Telephony.Mms.DATE, cache),
            serviceCenter = "",
            read = cursor.getIntValue(Telephony.Mms.READ, cache) == 1,
            threadId = cursor.getStringValue(Telephony.Mms.THREAD_ID, cache),
            type = cursor.getIntValue(Telephony.Mms.MESSAGE_BOX, cache),
            subscriptionId = cursor.getIntValue(Telephony.Mms.SUBSCRIPTION_ID, cache, -1),
            isMms = true,
            parts = readMmsParts(context, rawMmsId),
        )
    }

    private fun getCanonicalAddressesForThread(context: Context, threadId: String): List<String> {
        val conversationsUri = "content://mms-sms/conversations?simple=true".toUri()
        val recipientIds = context.contentResolver.queryCursor(
            conversationsUri,
            arrayOf(BaseColumns._ID, "recipient_ids"),
            "${BaseColumns._ID} = ?",
            arrayOf(threadId),
            null
        )?.find { cursor, cache ->
            cursor.getStringValue("recipient_ids", cache)
        } ?: return emptyList()

        if (recipientIds.isEmpty()) return emptyList()

        val addresses = SmsProviderContract.parseRecipientIds(recipientIds).mapNotNull { recipientId ->
            val canonicalUri = "content://mms-sms/canonical-address/$recipientId".toUri()
            context.contentResolver.queryCursor(
                canonicalUri,
                arrayOf("address"),
            )?.find { cursor, cache ->
                cursor.getStringValue("address", cache)
            }?.takeIf(String::isNotEmpty)
        }
        return addresses
    }

    internal fun readMmsAddresses(context: Context, mmsId: String): List<MmsAddressFacts> {
        val addrUri = "content://mms/$mmsId/addr".toUri()
        val colType = Telephony.Mms.Addr.TYPE
        val colAddress = Telephony.Mms.Addr.ADDRESS
        return context.contentResolver.queryCursor(
            addrUri,
            arrayOf(colAddress, colType),
            "$colType = ? OR $colType = ?",
            arrayOf(MMS_ADDR_TYPE_FROM.toString(), MMS_ADDR_TYPE_TO.toString()),
            null,
        )?.map { cursor, cache ->
            MmsAddressFacts(
                cursor.getStringValue(colAddress, cache),
                cursor.getIntValue(colType, cache) == MMS_ADDR_TYPE_FROM,
            )
        }.orEmpty()
    }

    internal fun readMmsParts(context: Context, mmsId: String): List<MmsPartFacts> =
        context.contentResolver.queryCursor(
            mmsPartUri,
            arrayOf(Telephony.Mms.Part._ID, Telephony.Mms.Part.CONTENT_TYPE,
                Telephony.Mms.Part.NAME, Telephony.Mms.Part.FILENAME,
                Telephony.Mms.Part._DATA, Telephony.Mms.Part.TEXT),
            "mid = ?", arrayOf(mmsId), null,
        )?.map { cursor, cache ->
            val partId = cursor.getStringValue(Telephony.Mms.Part._ID, cache)
            val type = cursor.getStringValue(Telephony.Mms.Part.CONTENT_TYPE, cache)
            val data = cursor.getStringValue(Telephony.Mms.Part._DATA, cache)
            val inline = cursor.getStringValue(Telephony.Mms.Part.TEXT, cache)
            MmsPartFacts(
                path = "content://mms/part/$partId",
                contentType = type,
                name = cursor.getStringValue(Telephony.Mms.Part.NAME, cache),
                fileName = cursor.getStringValue(Telephony.Mms.Part.FILENAME, cache),
                hasData = data.isNotEmpty(),
                text = if (type.equals("text/plain", ignoreCase = true)) readMmsTextPart(context, partId, inline, data) else inline,
            )
        }.orEmpty()

    private fun readMmsTextPart(context: Context, partId: String, inlineText: String, dataColumn: String): String {
        if (inlineText.isNotEmpty() || dataColumn.isEmpty()) return inlineText
        return runCatching {
            context.contentResolver.openInputStream(Uri.parse("content://mms/part/$partId"))
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
        }.getOrDefault("")
    }

    private fun mmsTextFacts(context: Context): Map<String, List<String>> {
        val textByMmsId = linkedMapOf<String, MutableList<String>>()
        context.contentResolver.queryCursor(
            mmsPartUri,
            arrayOf(
                Telephony.Mms.Part._ID,
                "mid",
                Telephony.Mms.Part.TEXT,
                Telephony.Mms.Part._DATA,
            ),
            "${Telephony.Mms.Part.CONTENT_TYPE} = ?",
            arrayOf("text/plain"),
            null,
        )?.use { cursor ->
            val cache = mutableMapOf<String, Int>()
            while (cursor.moveToNext()) {
                val partId = cursor.getStringValue(Telephony.Mms.Part._ID, cache)
                val mmsId = cursor.getStringValue("mid", cache)
                val text = readMmsTextPart(
                    context,
                    partId,
                    cursor.getStringValue(Telephony.Mms.Part.TEXT, cache),
                    cursor.getStringValue(Telephony.Mms.Part._DATA, cache),
                )
                textByMmsId.getOrPut(mmsId) { mutableListOf() }.add(text)
            }
        }
        return textByMmsId
    }

    suspend fun facts(context: Context, method: String, params: JsonObject): JsonElement = withIO {
        if (context.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("READ_SMS permission revoked")
        }
        if (method == "systemMmsTextFacts") return@withIO JsonHelper.jsonEncodeToElement(mmsTextFacts(context))
        if (method == "systemSmsConversationFacts") return@withIO SmsConversationHelper.facts(context, params)
        val request = if (method == "systemSmsRowsFacts" || method == "systemSmsThreadFacts") {
            JsonHelper.jsonDecodeFromElement<SmsFactsRequest>(params)
        } else {
            SmsFactsRequest(JsonHelper.jsonDecodeFromElement<SmsQueryPlans>(params))
        }
        val plans = request.plans
        val smsWhere = plans.sms.where()
        val mmsWhere = plans.mms?.where()
        when (method) {
            "systemSmsThreadFacts" -> {
                val hits=mutableListOf<Pair<String,String>>()
                context.contentResolver.queryCursor(smsUri,arrayOf(Telephony.Sms.THREAD_ID,Telephony.Sms.DATE),smsWhere.toSelection(),smsWhere.args.toTypedArray(),null)?.use { cursor ->
                    val cache=mutableMapOf<String,Int>(); while(cursor.moveToNext()) {
                        hits.add(cursor.getStringValue(Telephony.Sms.THREAD_ID,cache) to cursor.getTimeValue(Telephony.Sms.DATE,cache).toString())
                    }
                }
                if(mmsWhere!=null) context.contentResolver.queryCursor(mmsUri,arrayOf(Telephony.Mms.THREAD_ID,Telephony.Mms.DATE),mmsWhere.toSelection(),mmsWhere.args.toTypedArray(),null)?.use { cursor ->
                    val cache=mutableMapOf<String,Int>(); while(cursor.moveToNext()) {
                        hits.add(cursor.getStringValue(Telephony.Mms.THREAD_ID,cache) to cursor.getTimeSecondsValue(Telephony.Mms.DATE,cache).toString())
                    }
                }
                JsonHelper.jsonEncodeToElement(hits)
            }
            "systemSmsCountFacts" -> JsonHelper.jsonEncodeToElement(SmsCountFacts(
                sms = queryCount(context, smsUri, smsWhere.toSelection(), smsWhere.args.toTypedArray()),
                mms = mmsWhere?.let { queryCount(context, mmsUri, it.toSelection(), it.args.toTypedArray()) } ?: 0,
            ))
            "systemSmsIdsFacts" -> {
                val smsIds = context.contentResolver.queryCursor(smsUri, arrayOf(BaseColumns._ID), smsWhere.toSelection(), smsWhere.args.toTypedArray(), null)
                    ?.map { cursor, cache -> cursor.getStringValue(BaseColumns._ID, cache) }.orEmpty()
                val mmsIds = mmsWhere?.let { where ->
                    context.contentResolver.queryCursor(mmsUri, arrayOf(BaseColumns._ID), where.toSelection(), where.args.toTypedArray(), null)
                        ?.map { cursor, cache -> "mms_${cursor.getStringValue(BaseColumns._ID, cache)}" }
                }.orEmpty()
                JsonHelper.jsonEncodeToElement(smsIds + mmsIds)
            }
            "systemSmsRowsFacts" -> {
                val limit = requireNotNull(request.limit)
                require(limit in 1..4294967294L)
                val sms = context.contentResolver.queryCursor(smsUri, getProjection(), smsWhere.toSelection(), smsWhere.args.toTypedArray(), "${Telephony.Sms.DATE} DESC LIMIT $limit")
                    ?.map { cursor, cache -> cursorToSmsMessage(cursor, cache) }.orEmpty()
                val mms = mmsWhere?.let { where ->
                    context.contentResolver.queryCursor(mmsUri, arrayOf(Telephony.Mms._ID, Telephony.Mms.DATE, Telephony.Mms.THREAD_ID,
                        Telephony.Mms.MESSAGE_BOX, Telephony.Mms.READ, Telephony.Mms.SUBSCRIPTION_ID), where.toSelection(), where.args.toTypedArray(), "${Telephony.Mms.DATE} DESC LIMIT $limit")
                        ?.map { cursor, cache -> cursorToMmsMessage(context, cursor, cache) }
                }.orEmpty()
                val threadId = requireNotNull(plans.threadId)
                JsonHelper.jsonEncodeToElement(SmsRowsFacts(
                    items = sms + mms,
                    canonicalAddresses = if (threadId.isNotEmpty()) getCanonicalAddressesForThread(context, threadId) else emptyList(),
                    ownNumbers = getSims().map { it.number }.filter(String::isNotEmpty),
                ))
            }
            else -> error("Unsupported SMS facts")
        }
    }
}

@Serializable
private data class SmsFactsRequest(val plans: SmsQueryPlans, val limit: Long? = null)

@Serializable
private data class SmsQueryPlans(val sms: SmsWhere, val mms: SmsWhere? = null, val threadId: String? = null)

@Serializable
private data class SmsWhere(val clauses: List<String>, val args: List<String>) {
    fun where(): ContentWhere = ContentWhere().also { where ->
        clauses.forEach { where.add(it) }
        where.args.addAll(args)
    }
}
