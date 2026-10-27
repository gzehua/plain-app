package com.ismartcoding.plain.features.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ServiceState {
    @SerialName("stopped")
    OFF,
    @SerialName("started")
    ON,
    @SerialName("starting")
    STARTING,
    @SerialName("stopping")
    STOPPING,
    @SerialName("error")
    ERROR;

    fun isProcessing(): Boolean {
        return setOf(STARTING, STOPPING).contains(this)
    }
}
