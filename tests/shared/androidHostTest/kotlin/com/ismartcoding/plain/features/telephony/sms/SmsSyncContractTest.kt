package com.ismartcoding.plain.features.telephony.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsSyncContractTest {
    @Test
    fun `content MMS predicate includes sent and retrieved PDUs`() {
        assertEquals("m_type IN (128, 132)", SmsProviderContract.MMS_CONTENT_FILTER)
    }

    @Test
    fun `provider change bursts coalesce distinct URIs and clear after delivery`() {
        val changes = SmsProviderChangeBuffer()
        changes.start()

        assertEquals(true, changes.add("content://sms/1"))
        assertEquals(true, changes.add("content://mms/2"))
        assertEquals(true, changes.add("content://sms/1"))
        assertEquals(listOf("content://sms/1", "content://mms/2"), changes.drain())
        assertEquals(emptyList<String>(), changes.drain())
    }

    @Test
    fun `stopping provider changes clears queued work and rejects later changes`() {
        val changes = SmsProviderChangeBuffer()
        changes.start()
        changes.add("content://sms/1")

        changes.stop()

        assertEquals(emptyList<String>(), changes.drain())
        assertEquals(false, changes.add("content://mms/2"))
    }

    @Test
    fun `recipient IDs retain every distinct participant`() {
        assertEquals(
            listOf("7", "8", "9"),
            SmsProviderContract.parseRecipientIds(" 7  8 9 8 "),
        )
    }

    @Test
    fun `SMS PendingIntent identities are unique per request and part`() {
        val first = SmsProviderContract.smsSentIntentIdentity("com.example", "request-a", 0)
        val secondPart = SmsProviderContract.smsSentIntentIdentity("com.example", "request-a", 1)
        val secondRequest = SmsProviderContract.smsSentIntentIdentity("com.example", "request-b", 0)

        assertNotEquals(first, secondPart)
        assertNotEquals(first, secondRequest)
        assertTrue(first.action.contains("request-a.0"))
        assertTrue(first.data.endsWith("/request-a/0"))
    }

    @Test
    fun `large MMS ID selections are numeric literal chunks without bind arguments`() {
        val predicate = SmsProviderContract.numericIdPredicate(
            "_id",
            (1..1001).map(Int::toString),
        ).orEmpty()

        assertEquals(3, Regex("_id IN").findAll(predicate).count())
        assertFalse(predicate.contains('?'))
        assertTrue(predicate.contains("1001"))
        assertEquals(null, SmsProviderContract.numericIdPredicate("_id", listOf("1", "not-an-id")))
    }

}
