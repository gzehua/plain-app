package com.ismartcoding.plain

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.appcompat.app.AppCompatActivity

var mainActivity: AppCompatActivity? = null
private var applicationContext: Context? = null

val appContextValue: Context?
    get() = applicationContext

val appContextOrNull: Context?
    get() = applicationContext

val appContext: Context
    get() = applicationContext
        ?: error("setAppContext must be called before appContext is used")

fun setAppContext(context: Context, buildType: String = "", buildChannel: String = "") {
    applicationContext = context.applicationContext
    buildTypeValue = buildType
    buildChannelValue = buildChannel
}

fun isDebugBuild(): Boolean {
    val ctx = appContextOrNull ?: return false
    return (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
