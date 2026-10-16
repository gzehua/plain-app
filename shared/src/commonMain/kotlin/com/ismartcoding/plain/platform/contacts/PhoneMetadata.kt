package com.ismartcoding.plain.platform.contacts

import com.ismartcoding.plain.rust.http.request

import com.ismartcoding.plain.features.telephony.calls.PhoneLocaleFacts
import com.ismartcoding.plain.features.telephony.calls.PhoneMetadataFacts
import com.ismartcoding.plain.features.telephony.calls.PhoneMetadataRequest

internal expect fun phoneLocaleFacts(): PhoneLocaleFacts
internal expect fun phoneMetadata(request: PhoneMetadataRequest): PhoneMetadataFacts
