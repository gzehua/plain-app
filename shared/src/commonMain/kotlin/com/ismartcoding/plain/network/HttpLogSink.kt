package com.ismartcoding.plain.network

fun interface HttpLogSink {
    fun log(message: String)
}

var httpLogSink: HttpLogSink = HttpLogSink { println("[HttpLog] $it") }
