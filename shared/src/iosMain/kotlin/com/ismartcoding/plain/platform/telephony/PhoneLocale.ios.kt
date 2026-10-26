package com.ismartcoding.plain.platform.telephony

import com.ismartcoding.plain.features.telephony.calls.PhoneLocaleFacts
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.Foundation.preferredLanguages

@OptIn(ExperimentalForeignApi::class)
internal actual fun phoneLocaleFacts(): PhoneLocaleFacts {
    val locale = NSLocale.currentLocale
    val language = NSLocale.preferredLanguages.firstOrNull() as? String
    return PhoneLocaleFacts(locale.countryCode.orEmpty(), language ?: locale.localeIdentifier.replace('_', '-'))
}
