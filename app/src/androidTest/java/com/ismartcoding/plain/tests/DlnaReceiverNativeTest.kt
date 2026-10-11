package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.dlna.DlnaRendererState
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DlnaReceiverNativeTest {
    @Test
    fun rendererSnapshotLoadsThroughNativeBridge() = runBlocking {
        DlnaRendererState.refresh()
    }
}
