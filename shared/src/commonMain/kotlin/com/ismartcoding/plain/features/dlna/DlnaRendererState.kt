package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.dlna.DlnaMediaType
import com.ismartcoding.plain.lib.dlna.PendingCastRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.*
import com.ismartcoding.plain.platform.system.PlatformLock

object DlnaRendererState {
    /** Broadcast by the engine whenever the renderer state changes. */

    val isRunning = MutableStateFlow(false)
    val isRetrying = MutableStateFlow(false)
    val mediaUri = MutableStateFlow("")
    val mediaTitle = MutableStateFlow("")
    val mediaAlbumArtUri = MutableStateFlow("")
    val mediaType = MutableStateFlow(DlnaMediaType.UNKNOWN)
    val playbackState = MutableStateFlow(DlnaPlaybackState.NO_MEDIA_PRESENT)
    /** Non-null signals the player to seek to this position (milliseconds). */
    val seekTargetMs = MutableStateFlow<Long?>(null)
    /** Non-null when a cast request is shown to the user for confirmation. */
    val pendingCastRequest = MutableStateFlow<PendingCastRequest?>(null)
    /** Non-empty when the receiver failed to start on all attempted ports. */
    val startError = MutableStateFlow("")

    private val projectionLock = PlatformLock()
    private var version = -1L

    private suspend fun command(command: ReceiverCommand) {
        apply(RustDlnaReceiver.call(command))
    }
    suspend fun refresh() = command(ReceiverCommand.Snapshot)
    fun retry() { coIO { command(ReceiverCommand.Retry) } }
    fun acceptCastRequest(rememberChoice: Boolean) { coIO { command(ReceiverCommand.Accept(rememberChoice)) } }
    fun rejectCastRequest(rememberChoice: Boolean) { coIO { command(ReceiverCommand.Reject(rememberChoice)) } }

    internal fun apply(row: JsonObject) {
        val state = JsonHelper.jsonDecodeFromElement<ReceiverSnapshot>(row)
        projectionLock.withLock {
            if (state.version < version) return@withLock
            version = state.version
            isRunning.value = state.isRunning
            isRetrying.value = state.isRetrying
            mediaUri.value = state.mediaUri
            mediaTitle.value = state.mediaTitle
            mediaAlbumArtUri.value = state.mediaAlbumArtUri
            mediaType.value = state.mediaType
            playbackState.value = state.playbackState
            seekTargetMs.value = state.seekTargetMs
            startError.value = state.startError
            pendingCastRequest.value = state.pendingCastRequest
        }
    }
}
