package com.ismartcoding.plain.platform

import android.os.Build

fun isQPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q // android 10
fun isRPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R // android 11
fun isSPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // android 12
fun isTPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // android 13
fun isUPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
