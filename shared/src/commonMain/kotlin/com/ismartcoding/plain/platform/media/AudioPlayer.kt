package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.ui.permissions.checkNotificationPermission

import com.ismartcoding.plain.features.audio.AudioCommands
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.audio_notification_prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

interface AudioPlayer {
    val isPlayingFlow: StateFlow<Boolean>
    val currentPath: String
    fun bindRevision(revision: Long)
    fun restorePlayback(revision: Long, positionMs: Long)
    suspend fun load(audio: AudioItem, positionMs: Long, speed: Float, revision: Long)
    val progress: Long
    suspend fun seekTo(positionMs: Long)
    suspend fun pause()
    suspend fun play()
    suspend fun restartIfPlaying()
    suspend fun clear()
    suspend fun setPlaybackSpeed(speed: Float)
}

expect fun createAudioPlayer(): AudioPlayer

internal val audioPlayer: AudioPlayer by lazy { createAudioPlayer() }

fun audioIsPlayingFlow(): StateFlow<Boolean> = audioPlayer.isPlayingFlow

fun audioPlayerProgress(): Long = audioPlayer.progress

/** Suspending read for non-main threads (e.g. the HTTP server): the Android player is a Media3 MediaController which only allows calls on the app thread. */
suspend fun audioPlayerProgressAsync(): Long = withContext(Dispatchers.Main) { audioPlayer.progress }

fun audioSeekTo(positionMs: Long) = AudioCommands.submit("SEEK", positionMs = positionMs)

fun audioPause() = AudioCommands.submit("PAUSE")

fun audioPlay() = AudioCommands.submit("PLAY")

fun restartAudioIfPlaying() = AudioCommands.submit("RESTART")

fun playAudioWithNotificationCheck(path: String) {
    checkNotificationPermission(Res.string.audio_notification_prompt) {
        AudioCommands.playPath(path)
    }
}

fun audioJustPlay(audio: AudioItem) = AudioCommands.playTrack(audio)

fun audioJustPlayWithNotificationCheck(audio: AudioItem) {
    checkNotificationPermission(Res.string.audio_notification_prompt) {
        AudioCommands.playTrack(audio)
    }
}

fun audioSkipToPrevious() = AudioCommands.submit("PREVIOUS")

fun audioSkipToNext() = AudioCommands.submit("NEXT")

fun audioSetPlaybackSpeed(speed: Float) = AudioCommands.submit("SET_SPEED", speed = speed)
