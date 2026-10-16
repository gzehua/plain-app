package com.ismartcoding.plain.platform.files

import kotlinx.serialization.Serializable

@Serializable
internal data class ResourceStreamEnd(val kind: String = "end")
