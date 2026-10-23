package com.ismartcoding.plain.features.dlna.sender

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("action")
@Serializable
internal sealed class CastCommand {
    @Serializable @SerialName("startScan") data object StartScan : CastCommand()
    @Serializable @SerialName("stopScan") data object StopScan : CastCommand()
    @Serializable @SerialName("select") data class Select(val id: String) : CastCommand()
    @Serializable @SerialName("cast") data class Cast(val item: CastItem) : CastCommand()
    @Serializable @SerialName("play") data object Play : CastCommand()
    @Serializable @SerialName("pause") data object Pause : CastCommand()
    @Serializable @SerialName("seek") data class Seek(val positionMs: Long) : CastCommand()
    @Serializable @SerialName("stop") data object Stop : CastCommand()
    @Serializable @SerialName("exit") data object Exit : CastCommand()
    @Serializable @SerialName("add") data class Add(val item: CastItem) : CastCommand()
    @Serializable @SerialName("remove") data class Remove(val path: String) : CastCommand()
    @Serializable @SerialName("removeAt") data class RemoveAt(val index: Int) : CastCommand()
    @Serializable @SerialName("reorder") data class Reorder(val from: Int, val to: Int) : CastCommand()
    @Serializable @SerialName("clear") data object Clear : CastCommand()
}
