package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.rust.http.request

import com.ismartcoding.plain.features.telephony.calls.PhoneLocaleFacts
import com.ismartcoding.plain.features.telephony.calls.PhoneMetadataFacts
import com.ismartcoding.plain.features.telephony.calls.PhoneMetadataRequest

internal actual fun phoneLocaleFacts() = PhoneLocaleFacts("", "", false)
internal actual fun phoneMetadata(request: PhoneMetadataRequest) = PhoneMetadataFacts("", "", "", "")
