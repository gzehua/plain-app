package com.ismartcoding.plain.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpClientFactory {
    fun createImageLoaderClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
}
