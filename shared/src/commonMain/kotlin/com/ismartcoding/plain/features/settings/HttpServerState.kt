package com.ismartcoding.plain.features.settings

enum class HttpServerState {
    OFF,
    ON,
    STARTING,
    STOPPING,
    ERROR;

    fun isProcessing(): Boolean {
        return setOf(STARTING, STOPPING).contains(this)
    }
}
