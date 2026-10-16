package com.ismartcoding.plain.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.ismartcoding.plain.enums.PickFileTag
import com.ismartcoding.plain.events.PickFileResultEvent
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.ui.helpers.DialogHelper
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun PickImageEffect(imageUrl: MutableState<String>) {
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            if (event is PickFileResultEvent && event.tag == PickFileTag.EDITOR) {
                val source = event.uris.firstOrNull() ?: return@collect
                try {
                    val info = checkNotNull(queryPickedFileInfo(source)) { "Unable to read selected image" }
                    imageUrl.value = RustContentApi.mutate(
                        "importNoteImage(source: ${gql(source)}, fileName: ${gql(info.displayName)})", longRunning = true,
                    ).getValue("importNoteImage").jsonPrimitive.content
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    DialogHelper.showMessage(error.message.orEmpty())
                }
            }
        }
    }
}
