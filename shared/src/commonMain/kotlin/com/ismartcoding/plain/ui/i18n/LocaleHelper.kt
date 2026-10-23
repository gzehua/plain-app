package com.ismartcoding.plain.ui.i18n

import com.ismartcoding.plain.platform.system.Locale
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString as getComposeString
import org.jetbrains.compose.resources.getPluralString as getComposePluralString
import com.ismartcoding.plain.platform.system.currentLocale as systemCurrentLocale
import kotlinx.coroutines.runBlocking

object LocaleHelper {
    fun currentLocale(): Locale = systemCurrentLocale()

    suspend fun getStringAsync(resource: StringResource): String = getComposeString(resource)

    suspend fun getStringFAsync(resource: StringResource, vararg formatArguments: Any): String =
        getComposeString(resource, *formatArguments)

    suspend fun getPluralStringAsync(resource: PluralStringResource, count: Int): String =
        getComposePluralString(resource, count, count)

    fun getString(resource: StringResource): String = runBlocking { getComposeString(resource) }

    fun getStringF(resource: StringResource, vararg formatArguments: Any): String =
        runBlocking { getComposeString(resource, *formatArguments) }
}
