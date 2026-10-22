package com.ismartcoding.plain.rust

import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpStatus(val state: String, val message: String = "")
