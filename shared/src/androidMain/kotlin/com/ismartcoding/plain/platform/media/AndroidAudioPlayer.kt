package com.ismartcoding.plain.platform.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.ismartcoding.plain.platform.system.awaitPlatformFuture
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.features.audio.AudioAction
import com.ismartcoding.plain.app.events.AudioActionEvent
import com.ismartcoding.plain.features.audio.AudioQueueClient
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.services.AudioPlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object AndroidAudioPlayer {
    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    fun isPlaying(): Boolean {
        return player?.isPlaying == true
    }

    /**
     * The flow exposes *playback intent* (playWhenReady), not "audio is coming
     * out right now": it stays true while the player switches tracks, so play
     * buttons never flicker during a skip. False only when paused, stopped or
     * uninitialized. STATE_ENDED keeps playWhenReady true — skipTo pauses the
     * player explicitly when the queue runs dry.
     */
    private fun refreshPlayingState() {
        val p = player ?: return
        _isPlayingFlow.value = p.playWhenReady && p.playbackState != Player.STATE_IDLE
    }

    private var activeRevision = -1L
    private var playerListener: Player.Listener = listener(activeRevision)

    fun bindRevision(revision: Long) {
        player?.removeListener(playerListener)
        activeRevision = revision
        playerListener = listener(revision)
        player?.addListener(playerListener)
    }

    private fun listener(revision: Long) = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            if (revision != activeRevision) return
            pendingStart = null
            player?.pause()
            refreshPlayingState()
            LogCat.e("Audio playback: ${error.message}")
            setChangedNotify(AudioAction.NOT_FOUND)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (revision != activeRevision) return
            LogCat.d("Player.isPlaying changed to: $isPlaying")
            refreshPlayingState()
            if (isPlaying) {
                val audio = pendingStart?.takeIf { it.first.path == currentPath }
                if (audio != null) {
                    pendingStart = null
                    coIO { AudioQueueClient.onStarted(audio.first, audio.second) }
                }
            }
            if (!isPlaying && player != null) {
                TempData.audioPlayPosition = player?.currentPosition ?: 0
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (revision != activeRevision) return
            refreshPlayingState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (revision != activeRevision) return
            refreshPlayingState()
            if (playbackState == Player.STATE_ENDED && player?.playbackState == Player.STATE_ENDED) {
                val path = currentPath
                if (path.isNotEmpty()) coIO { AudioQueueClient.onCompleted(path, revision) }
            }
        }
    }

    private var controller: MediaController? = null
    private var player: Player? = null
    private var pendingStart: Pair<AudioItem, Long>? = null
    val currentPath: String get() = player?.currentMediaItem?.mediaId.orEmpty()
    var playerProgress: Long = 0 // player progress in milliseconds
        get() {
            val currentPlayer = player
            if (currentPlayer == null) {
                return TempData.audioPlayPosition
            }

            val currentPositionMs = currentPlayer.currentPosition
            // Keep UI stable right after seek when player may briefly report 0.
            if (currentPositionMs == 0L && TempData.audioPlayPosition > 0L) {
                return TempData.audioPlayPosition
            }
            return currentPositionMs
        }

    private val initializationMutex = Mutex()
    private var initialization: ListenableFuture<MediaController>? = null

    private suspend fun ensurePlayer(context: Context) = initializationMutex.withLock {
        if (player != null) return@withLock
        val sessionToken = SessionToken(context, ComponentName(context, AudioPlayerService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        initialization = future
        try {
            val connected = awaitPlatformFuture(future, ContextCompat.getMainExecutor(context)) { it.release() }
            try {
                val native = checkNotNull(AudioPlayerService.nativePlayer) { "Audio service player is unavailable" }
                native.addListener(playerListener)
                controller = connected
                player = native
                refreshPlayingState()
            } catch (error: Exception) {
                connected.release()
                throw error
            }
        } finally {
            initialization = null
        }
    }

    suspend fun load(context: Context, audio: AudioItem, positionMs: Long, speed: Float, revision: Long) = withContext(Dispatchers.Main) {
        withTimeout(15_000) { ensurePlayer(context) }
        TempData.audioPlayPosition = positionMs
        doPlay(audio, speed, revision)
    }

    suspend fun play() = withContext(Dispatchers.Main) { player?.play(); Unit }

    suspend fun seekTo(positionMs: Long) = withContext(Dispatchers.Main) {
        TempData.audioPlayPosition = positionMs
        player?.seekTo(positionMs)
        Unit
    }

    suspend fun pause() = withContext(Dispatchers.Main) {
        TempData.audioPlayPosition = player?.currentPosition ?: 0
        player?.pause()
        Unit
    }

    suspend fun clear() = withContext(Dispatchers.Main) {
        player?.pause()
        pendingStart = null
        player?.clearMediaItems()
        TempData.audioPlayPosition = 0
    }

    suspend fun setPlaybackSpeed(speed: Float) = withContext(Dispatchers.Main) {
        player?.setPlaybackSpeed(speed)
        Unit
    }

    fun release() {
        initialization?.cancel(false)
        player?.removeListener(playerListener)
        pendingStart = null
        player = null
        controller?.release()
        controller = null
        _isPlayingFlow.value = false
        TempData.audioPlayPosition = 0
    }

    private fun doPlay(
        audio: AudioItem,
        speed: Float,
        revision: Long,
    ) {
        pendingStart = audio to revision
        player?.setMediaItem(audio.toMediaItem())
        player?.prepare()
        player?.seekTo(TempData.audioPlayPosition)
        player?.setPlaybackSpeed(speed)
        player?.play()
    }

    fun setChangedNotify(action: AudioAction) {
        sendEvent(AudioActionEvent(action))
    }
}
