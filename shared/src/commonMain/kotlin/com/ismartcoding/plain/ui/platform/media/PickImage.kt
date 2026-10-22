package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.platform.files.queryPickedFileInfo
import com.ismartcoding.plain.network.request

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.app.events.PickFileResultEvent
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.ui.components.DialogHelper
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
                    imageUrl.value = RustGraphqlApi.mutate(
                        "importNoteImage(source: ${gql(source)}, fileName: ${gql(info.displayName)})",
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
