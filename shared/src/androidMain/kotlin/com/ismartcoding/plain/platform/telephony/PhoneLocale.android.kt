package com.ismartcoding.plain.platform.telephony

import android.content.Context
import android.telephony.TelephonyManager
import com.ismartcoding.plain.appContextOrNull
import com.ismartcoding.plain.features.telephony.calls.PhoneLocaleFacts
import java.util.Locale

internal actual fun phoneLocaleFacts(): PhoneLocaleFacts {
    val telephony = appContextOrNull?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val locale = Locale.getDefault()
    return PhoneLocaleFacts(locale.country, locale.toLanguageTag(), telephony?.networkCountryIso.orEmpty(), telephony?.simCountryIso.orEmpty())
}
