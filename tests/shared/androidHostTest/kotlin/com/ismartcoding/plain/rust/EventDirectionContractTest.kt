package com.ismartcoding.plain.rust

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EventDirectionContractTest {
    @Test fun nativeReportsAndSubscriptionsUseSeparateTypedEntrypoints() {
        val root = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .first { File(it, "shared/src/commonMain").isDirectory }
        val sources = File(root, "shared/src/commonMain/kotlin/com/ismartcoding/plain")
        assertFalse(File(sources, "rust/RustEventHandler.kt").exists())
        assertFalse(File(sources, "events/HostEvents.kt").exists())
        val reports = File(sources, "rust/native/RustHostReports.kt").readText()
        assertTrue(reports.contains("reportScreenMirrorVideo"))
        assertTrue(reports.contains("reportScreenMirrorAudio"))
        assertTrue(reports.contains("reportPermissions"))
        val subscriptions = File(sources, "rust/graphql/RustGraphqlSubscriptions.kt").readText()
        assertTrue(subscriptions.contains("graphqlSubscriptionOpen"))
        assertFalse(subscriptions.contains("http://"))
        assertFalse(subscriptions.contains("https://"))
    }
}
