package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Certificate(
    var issuer: String,
    var subject: String,
    var serialNumber: String,
    var validFrom: Instant,
    var validTo: Instant,
)
