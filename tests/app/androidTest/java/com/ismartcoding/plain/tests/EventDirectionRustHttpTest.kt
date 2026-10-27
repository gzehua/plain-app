package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDirectionRustHttpTest {
    @Test fun platformReportsAreNotExposedAsMainGraphqlMutations() = runBlocking {
        for (field in listOf("reportPermissions", "reportScreenMirrorVideo", "reportClipboard")) {
            assertTrue("$field must not be a main-schema mutation", runCatching { RustGraphqlApi.mutate("$field") }.isFailure)
        }
    }
}
