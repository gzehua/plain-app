package com.ismartcoding.plain.ui.audio

import com.ismartcoding.plain.features.audio.toPlaylistAudio
import com.ismartcoding.plain.api.graphql.models.Audio as AudioTrack
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.app.events.ClearAudioQueueEvent
import com.ismartcoding.plain.features.audio.AudioQueueClient
import com.ismartcoding.plain.api.graphql.models.Audio
import com.ismartcoding.plain.api.graphql.models.AudioItem
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.media.audioJustPlay
import com.ismartcoding.plain.preferences.*

class AudioQueueViewModel : ViewModel(), AudioQueueViewModelBase {
    val queueItems = mutableStateOf<List<AudioItem>>(listOf())
    val queueCount = mutableStateOf(0)
    val noMore = mutableStateOf(false)

    val canReorder = mutableStateOf(false)

    val queuedMediaIds = mutableStateOf<Set<String>>(emptySet())

    val selectedMediaId = mutableStateOf<String?>(null)
    override val selectedPath = mutableStateOf("")

    private val pageLimit = 200

    suspend fun loadAsync() {
        val playback = AudioQueueClient.playback()
        selectedPath.value = playback.path.orEmpty()
        selectedMediaId.value = playback.mediaId
        refreshWindow()
    }

    fun isInQueue(mediaId: String): Boolean = mediaId in queuedMediaIds.value

    suspend fun moreAsync() {
        if (noMore.value) return
        // Refetch the whole window instead of offset-appending: manual queue
        // mutations shift ranks, so an offset append can overlap the old window
        // and duplicate tracks.
        refreshWindow(queueItems.value.size + pageLimit)
    }

    suspend fun addAsync(items: List<AudioTrack>) {
        val audio = items.map { it.toPlaylistAudio() }
        AudioQueueClient.enqueue(audio)
        if (selectedPath.value.isEmpty()) {
            setCurrentPlaying(audio.firstOrNull())
        }
        refreshWindow()
    }

    suspend fun playSingleAsync(audio: AudioItem) {
        AudioQueueClient.clearQueue()
        AudioQueueClient.enqueue(listOf(audio))
        audioJustPlay(audio)
        selectedPath.value = audio.path
        selectedMediaId.value = audio.mediaId
        refreshWindow()
    }

    suspend fun clearAsync() {
        AudioQueueClient.clearQueue()
        selectedPath.value = ""
        selectedMediaId.value = null
        refreshWindow()
        sendEvent(ClearAudioQueueEvent())
    }

    suspend fun onStarted(audio: AudioItem) {
        setCurrentPlaying(audio)
        refreshWindow()
    }

    private suspend fun setCurrentPlaying(audio: AudioItem?) {
        if (audio == null || !audio.mediaId.isNullOrEmpty()) AudioQueueClient.setCurrent(audio?.mediaId)
        selectedPath.value = audio?.path.orEmpty()
        selectedMediaId.value = audio?.mediaId
    }

    suspend fun playAsync(item: AudioTrack) {
        val audio = item.toPlaylistAudio()
        audioJustPlay(audio)
        setCurrentPlaying(audio)
        refreshWindow()
    }

    suspend fun enqueueAndPlayAsync(item: AudioTrack) {
        val audio = item.toPlaylistAudio()
        AudioQueueClient.enqueue(listOf(audio))
        audioJustPlay(audio)
        setCurrentPlaying(audio)
        refreshWindow()
    }

    suspend fun removeAsync(mediaId: String) {
        AudioQueueClient.removeQueued(mediaId)
        val playback = AudioQueueClient.playback()
        selectedPath.value = playback.path.orEmpty()
        selectedMediaId.value = playback.mediaId
        refreshWindow()
    }

    suspend fun reorder(from: Int, to: Int) {
        val list = queueItems.value.toMutableList()
        if (from !in list.indices || to !in list.indices) return
        list.add(to,list.removeAt(from))
        AudioQueueClient.reorderQueued(list.map { requireNotNull(it.mediaId) })
        queueItems.value = list
    }

    private suspend fun refreshWindow(target: Int = pageLimit) {
        val total = AudioQueueClient.queueTotal()
        val window = AudioQueueClient.queuePage(0, maxOf(target, pageLimit))
        queueItems.value = window
        queueCount.value = total
        noMore.value = window.size >= total
        canReorder.value = true
        queuedMediaIds.value = AudioQueueClient.queuedMediaIds()
    }
}
