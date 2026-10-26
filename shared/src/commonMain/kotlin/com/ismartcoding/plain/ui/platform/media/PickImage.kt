package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.rust.native.RustNativeApi

import com.ismartcoding.plain.platform.files.queryPickedFileInfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.events.PickFileResultEvent
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.CancellationException

@Composable
fun PickImageEffect(imageUrl: MutableState<String>) {
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            if (event is PickFileResultEvent && event.tag == PickFileTag.EDITOR) {
                val source = event.uris.firstOrNull() ?: return@collect
                try {
                    val info = checkNotNull(queryPickedFileInfo(source)) { "Unable to read selected image" }
                    imageUrl.value = RustNativeApi.importNoteImage(source, info.displayName)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    DialogHelper.showMessage(error.message.orEmpty())
                }
            }
        }
    }
}
