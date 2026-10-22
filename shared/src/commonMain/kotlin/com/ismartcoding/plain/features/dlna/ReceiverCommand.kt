package com.ismartcoding.plain.features.dlna

internal sealed class ReceiverCommand {
    data class RemoveRule(val ip: String, val allowed: Boolean) : ReceiverCommand()
    data object Snapshot : ReceiverCommand()
    data object Retry : ReceiverCommand()
    data class Accept(val remember: Boolean) : ReceiverCommand()
    data class Reject(val remember: Boolean) : ReceiverCommand()
}
