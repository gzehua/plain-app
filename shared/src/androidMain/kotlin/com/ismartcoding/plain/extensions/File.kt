package com.ismartcoding.plain.extensions

import java.io.File

fun File.getDirectChildrenCount(countHiddenItems: Boolean): Int {
    if (countHiddenItems) {
        return list()?.size ?: 0
    }
    return list()?.filter {
        !it.startsWith('.')
    }?.size ?: 0
}
