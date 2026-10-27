package com.ismartcoding.plain.rust.graphql

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal sealed interface GraphqlSubscriptionCommand {
    @Serializable @SerialName("subscribe")
    data class Subscribe(val id: String, val payload: GraphqlRequest) : GraphqlSubscriptionCommand
    @Serializable @SerialName("complete")
    data class Complete(val id: String) : GraphqlSubscriptionCommand
}
@Serializable
internal data class GraphqlSubscriptionMessage(val type: String, val id: String? = null, val payload: JsonElement? = null)
@Serializable
internal data class NativeSubscriptionReply(val handle: Long? = null, val value: String? = null, val error: String? = null)
