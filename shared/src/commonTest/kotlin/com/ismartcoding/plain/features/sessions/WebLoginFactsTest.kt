package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.lib.JsonHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WebLoginFactsTest {
    private val metadata = """"browserName":"Fixture","browserVersion":"1","osName":"Fixture","osVersion":"1","isMobile":false"""

    @Test
    fun passwordlessApprovalKeepsTheServerVerificationCodeWithoutRequiringAPassword() {
        val facts = JsonHelper.jsonDecode<WebLoginRequestFacts>("""{"clientId":"browser","requestId":"request","clientIp":"127.0.0.1","request":{$metadata,"verificationCode":"01234567 89abcdef 01234567 89abcdef","loginContext":"context"}}""")
        assertEquals("", facts.request.password)
        assertEquals("01234567 89abcdef 01234567 89abcdef", facts.request.verificationCode)
        assertEquals("request", facts.requestId)
    }

    @Test
    fun completedLoginFactsDoNotNeedTheRemovedPasswordField() {
        val facts = JsonHelper.jsonDecode<WebLoginCompletedFacts>("""{"request":{$metadata},"chatPaired":false,"clientIp":"127.0.0.1"}""")
        assertEquals("", facts.request.password)
        assertEquals("", facts.request.verificationCode)
        assertFalse(facts.chatPaired)
    }

    @Test
    fun closingFactsIdentifyOnlyTheirOwnPendingRequest() {
        val facts = JsonHelper.jsonDecode<WebLoginClosedFacts>("""{"requestId":"request"}""")
        assertEquals("request", facts.requestId)
        assertEquals("""{"requestId":"request"}""", JsonHelper.jsonEncodeToElement(facts).toString())
    }
}
