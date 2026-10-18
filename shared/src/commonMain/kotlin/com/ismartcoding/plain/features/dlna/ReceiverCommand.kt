package com.ismartcoding.plain.features.dlna

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@JsonClassDiscriminator("action")
@Serializable
internal sealed class ReceiverCommand {
    @Serializable @SerialName("REMOVE_RULE") data class RemoveRule(val ip: String, val allowed: Boolean) : ReceiverCommand()
    @Serializable @SerialName("SNAPSHOT") data object Snapshot : ReceiverCommand()
    @Serializable @SerialName("RETRY") data object Retry : ReceiverCommand()
    @Serializable @SerialName("ACCEPT") data class Accept(val remember: Boolean) : ReceiverCommand()
    @Serializable @SerialName("REJECT") data class Reject(val remember: Boolean) : ReceiverCommand()
}
