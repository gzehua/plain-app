package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.system.AppFeatureType

/** Publishes the user-picked tools as launcher long-press shortcuts; no-op where unsupported. */
expect fun publishLauncherShortcuts(tools: List<AppFeatureType>)
