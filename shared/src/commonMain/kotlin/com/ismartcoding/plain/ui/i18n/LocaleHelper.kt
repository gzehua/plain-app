package com.ismartcoding.plain.ui.i18n

import com.ismartcoding.plain.platform.system.Locale
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString as getComposeString
import org.jetbrains.compose.resources.getPluralString as getComposePluralString

object LocaleHelper {
    fun currentLocale(): Locale = com.ismartcoding.plain.platform.system.currentLocale()

    suspend fun getStringAsync(resource: StringResource): String = getComposeString(resource)

    suspend fun getStringFAsync(resource: StringResource, vararg formatArguments: Any): String =
        getComposeString(resource, *formatArguments)

    suspend fun getPluralStringAsync(resource: PluralStringResource, count: Int): String =
        getComposePluralString(resource, count, count)

    fun getString(resource: StringResource): String = kotlinx.coroutines.runBlocking { getComposeString(resource) }

    fun getStringF(resource: StringResource, vararg formatArguments: Any): String =
        kotlinx.coroutines.runBlocking { getComposeString(resource, *formatArguments) }
}
