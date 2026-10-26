package com.ismartcoding.plain.platform.system

expect fun isAppForegrounded(): Boolean

internal expect suspend fun clearMediaCache()
