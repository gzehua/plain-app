package com.ismartcoding.plain.features.shares

import kotlinx.serialization.Serializable

@Serializable
enum class ShareBatchType { FILE, ZIP, SYNC, MULTI }
