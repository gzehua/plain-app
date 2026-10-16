package com.ismartcoding.plain.features.dlna.sender

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.db.IMedia
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.platform.system.PlatformLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal object RustDlnaSender {
    private val projectionLock = PlatformLock()
    private var version = -1L
    var currentDevice: DlnaDevice? = null
        private set
    private val deviceState = MutableStateFlow<List<DlnaDevice>>(emptyList())
    val devices = deviceState.asStateFlow()
    private val itemState = MutableStateFlow<List<IMedia>>(emptyList())
    val items = itemState.asStateFlow()
    private val uriState = MutableStateFlow("")
    val currentUri = uriState.asStateFlow()
    val isPlaying = MutableStateFlow(false)
    val progressMs = MutableStateFlow(0f)
    val durationMs = MutableStateFlow(0f)
    val supportsCallback = MutableStateFlow(false)

    private const val fields = "version devices { id hostAddress name } currentDevice { id hostAddress name } items { path title albumArt artist durationMs audio } currentUri playing progressMs durationMs supportsCallback"
    suspend fun call(command: CastCommand) {
        val operation = when (command) {
            CastCommand.Snapshot -> null
            CastCommand.StartScan -> "startDlnaScan"
            CastCommand.StopScan -> "stopDlnaScan"
            is CastCommand.Select -> "selectDlnaDevice(id: ${gql(command.id)})"
            is CastCommand.Cast -> "castDlnaMedia(item: ${input(command.item)})"
            CastCommand.Play -> "playDlna"
            CastCommand.Pause -> "pauseDlna"
            is CastCommand.Seek -> "seekDlna(positionMs: ${command.positionMs})"
            CastCommand.Stop -> "stopDlna"
            CastCommand.Exit -> "exitDlna"
            is CastCommand.Add -> "addDlnaItem(item: ${input(command.item)})"
            is CastCommand.Remove -> "removeDlnaItem(path: ${gql(command.path)})"
            is CastCommand.RemoveAt -> "removeDlnaItemAt(index: ${command.index})"
            is CastCommand.Reorder -> "reorderDlnaItems(from: ${command.from}, to: ${command.to})"
            CastCommand.Clear -> "clearDlnaItems"
        }
        val row = if (operation == null) {
            PlainRust.mainQuery("dlnaSenderState { $fields }").getValue("dlnaSenderState")
        } else {
            PlainRust.mainMutate("$operation { $fields }").getValue(operation.substringBefore('('))
        }
        apply(row.jsonObject)
    }
    private fun input(item: CastItem): String = "{path: ${gql(item.path)}, title: ${gql(item.title)}, albumArt: ${gql(item.albumArt)}, artist: ${gql(item.artist)}, durationMs: ${item.durationMs}, audio: ${item.audio}}"
    suspend fun refresh() = call(CastCommand.Snapshot)
    fun addItem(item: IMedia) { coIO { call(CastCommand.Add(CastItem.from(item))) } }
    fun removeItem(item: IMedia) { coIO { call(CastCommand.Remove(item.path)) } }
    fun startScan() { coIO { call(CastCommand.StartScan) } }
    fun stopScan() { coIO { call(CastCommand.StopScan) } }

    fun apply(row: JsonObject) {
        val state = JsonHelper.jsonDecodeFromElement<CastSnapshot>(row)
        projectionLock.withLock {
            if (state.version < version) return@withLock
            version = state.version
            deviceState.value = state.devices
            currentDevice = state.currentDevice
            itemState.value = state.items
            uriState.value = state.currentUri
            isPlaying.value = state.playing
            progressMs.value = state.progressMs.toFloat()
            durationMs.value = state.durationMs.toFloat()
            supportsCallback.value = state.supportsCallback
        }
    }
}
