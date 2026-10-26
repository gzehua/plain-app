package com.ismartcoding.plain.rust.host.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

fun interface AudioCatalogObserver {
    fun observe(scope: CoroutineScope): Job
}
