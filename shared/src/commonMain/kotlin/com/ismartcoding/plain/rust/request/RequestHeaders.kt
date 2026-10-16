package com.ismartcoding.plain.rust.request

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.platform.system.getAppVersion
import com.ismartcoding.plain.platform.system.getPlatformName
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun clientHeadersMap(): Map<String, String> = mapOf(
    "c-id" to TempData.clientId,
    "c-platform" to getPlatformName(),
    "c-version" to getAppVersion(),
)

@OptIn(ExperimentalEncodingApi::class)
fun clientHeadersWith(vararg extra: Pair<String, String>): Map<String, String> =
    clientHeadersMap() + extra
