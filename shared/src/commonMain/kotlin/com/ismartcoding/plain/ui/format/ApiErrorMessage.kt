package com.ismartcoding.plain.ui.format

import com.ismartcoding.plain.ui.format.errorMessage

import com.ismartcoding.plain.network.ApiResult
import com.ismartcoding.plain.i18n.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString as getComposeString

fun ApiResult.errorMessage(): String {
        return exception?.toString() ?: response?.toString() ?: runBlocking { getComposeString(Res.string.unknown) }
    }
