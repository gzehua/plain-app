package com.ismartcoding.plain.features.dlna

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@JsonClassDiscriminator("action")
@Serializable
internal sealed class ReceiverCommand {
    @Serializable @SerialName("removeRule") data class RemoveRule(val ip: String, val allowed: Boolean) : ReceiverCommand()
    @Serializable @SerialName("snapshot") data object Snapshot : ReceiverCommand()
    @Serializable @SerialName("retry") data object Retry : ReceiverCommand()
    @Serializable @SerialName("accept") data class Accept(val remember: Boolean) : ReceiverCommand()
    @Serializable @SerialName("reject") data class Reject(val remember: Boolean) : ReceiverCommand()
}
