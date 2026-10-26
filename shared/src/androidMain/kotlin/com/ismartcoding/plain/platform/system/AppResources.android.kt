package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.appContext

fun appResourceColor(name: String): Int =
    appContext.resources.getIdentifier(name, "color", appContext.packageName)

fun appResourceDrawable(name: String): Int =
    appContext.resources.getIdentifier(name, "drawable", appContext.packageName)

fun appResourceMipmap(name: String): Int =
    appContext.resources.getIdentifier(name, "mipmap", appContext.packageName)
