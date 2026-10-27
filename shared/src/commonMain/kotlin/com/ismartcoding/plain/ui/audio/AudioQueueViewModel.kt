package com.ismartcoding.plain.ui.audio

import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filter
import com.ismartcoding.plain.rust.graphql.AppSubscriptions
import com.ismartcoding.plain.rust.graphql.changes
import com.ismartcoding.plain.features.audio.toPlaylistAudio
import com.ismartcoding.plain.models.Audio as AudioTrack
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.ismartcoding.plain.features.audio.AudioQueueClient
import com.ismartcoding.plain.models.AudioItem
import com.ismartcoding.plain.platform.media.audioJustPlay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.ismartcoding.plain.lib.logcat.LogCat

class AudioQueueViewModel : ViewModel() {
    val audioPlayerVisible = MutableStateFlow(false)
    val queueItems = mutableStateOf<List<AudioItem>>(listOf())
    val queueCount = mutableStateOf(0)
    val noMore = mutableStateOf(false)

    val canReorder = mutableStateOf(false)

    val queuedMediaIds = mutableStateOf<Set<String>>(emptySet())

    val selectedMediaId = mutableStateOf<String?>(null)
    val selectedPath = mutableStateOf("")

    private val pageLimit = 200

    init {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            merge(
                AppSubscriptions.contentChanged.changes().map { false },
                AppSubscriptions.audioPlaybackChanged.changes().map { true },
                AppSubscriptions.audioLibraryChanged.changes().map { false },
            ).collect { playbackChanged ->
                try {
                    when (playbackChanged) {
                        true -> {
                            val playback = AudioQueueClient.playback()
                            selectedPath.value = playback.path.orEmpty()
                            selectedMediaId.value = playback.mediaId
                        }
                        false -> loadAsync()
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    LogCat.e("Refresh audio queue", error)
                }
            }
        }
    }

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
        loadAsync()
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
    }

    suspend fun onStarted(audio: AudioItem) {
        setCurrentPlaying(audio)
        refreshWindow()
    }

    private fun setCurrentPlaying(audio: AudioItem?) {
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
