package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.audio.toPlaylistAudio
import com.ismartcoding.plain.graphql.models.Audio as AudioTrack
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.events.ClearAudioQueueEvent
import com.ismartcoding.plain.features.audio.AudioQueueManager
import com.ismartcoding.plain.graphql.models.Audio
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.audioJustPlay
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
        val playback = AudioQueueManager.playback()
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
        AudioQueueManager.enqueue(audio)
        if (selectedPath.value.isEmpty()) {
            setCurrentPlaying(audio.firstOrNull())
        }
        refreshWindow()
    }

    suspend fun playSingleAsync(audio: AudioItem) {
        AudioQueueManager.clearQueue()
        AudioQueueManager.enqueue(listOf(audio))
        audioJustPlay(audio)
        selectedPath.value = audio.path
        selectedMediaId.value = audio.mediaId
        refreshWindow()
    }

    suspend fun clearAsync() {
        AudioQueueManager.clearQueue()
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
        if (audio == null || !audio.mediaId.isNullOrEmpty()) AudioQueueManager.setCurrent(audio?.mediaId)
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
        AudioQueueManager.enqueue(listOf(audio))
        audioJustPlay(audio)
        setCurrentPlaying(audio)
        refreshWindow()
    }

    suspend fun removeAsync(mediaId: String) {
        AudioQueueManager.removeQueued(mediaId)
        val playback = AudioQueueManager.playback()
        selectedPath.value = playback.path.orEmpty()
        selectedMediaId.value = playback.mediaId
        refreshWindow()
    }

    suspend fun reorder(from: Int, to: Int) {
        val list = queueItems.value.toMutableList()
        if (from !in list.indices || to !in list.indices) return
        list.add(to,list.removeAt(from))
        AudioQueueManager.reorderQueued(list.map { requireNotNull(it.mediaId) })
        queueItems.value = list
    }

    private suspend fun refreshWindow(target: Int = pageLimit) {
        val total = AudioQueueManager.queueTotal()
        val window = AudioQueueManager.queuePage(0, maxOf(target, pageLimit))
        queueItems.value = window
        queueCount.value = total
        noMore.value = window.size >= total
        canReorder.value = true
        queuedMediaIds.value = AudioQueueManager.queuedMediaIds()
    }
}
