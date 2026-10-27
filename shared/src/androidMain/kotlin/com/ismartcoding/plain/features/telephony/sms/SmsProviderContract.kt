package com.ismartcoding.plain.features.telephony.sms

object SmsProviderContract {
    const val MMS_PDU_SEND_REQ = 128
    const val MMS_PDU_RETRIEVE_CONF = 132
    const val MMS_CONTENT_FILTER = "m_type IN ($MMS_PDU_SEND_REQ, $MMS_PDU_RETRIEVE_CONF)"

    data class SmsSentIntentIdentity(
        val action: String,
        val data: String,
    )

    fun parseRecipientIds(value: String): List<String> {
        return value.trim()
            .split(Regex("\\s+"))
            .filter(String::isNotEmpty)
            .distinct()
    }

    fun smsSentIntentIdentity(packageName: String, requestId: String, partIndex: Int): SmsSentIntentIdentity {
        return SmsSentIntentIdentity(
            action = "$packageName.SMS_SENT.$requestId.$partIndex",
            data = "plainapp://sms/sent/$requestId/$partIndex",
        )
    }

    fun numericIdPredicate(
        field: String,
        values: Collection<String>,
        chunkSize: Int = 500,
    ): String? {
        require(chunkSize > 0)
        val ids = values.distinct()
        if (ids.isEmpty() || ids.any { id -> id.isEmpty() || id.any { !it.isDigit() } }) return null
        return ids.chunked(chunkSize).joinToString(
            separator = " OR ",
            prefix = "(",
            postfix = ")",
        ) { chunk -> "$field IN (${chunk.joinToString(",")})" }
    }

}
