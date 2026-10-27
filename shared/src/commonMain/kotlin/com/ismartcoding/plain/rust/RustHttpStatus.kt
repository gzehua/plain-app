package com.ismartcoding.plain.rust

import com.ismartcoding.plain.features.settings.ServiceState
import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpStatus(
    val state: ServiceState,
    val message: String = "",
    val httpPort: Int = 0,
    val httpsPort: Int = 0,
)
