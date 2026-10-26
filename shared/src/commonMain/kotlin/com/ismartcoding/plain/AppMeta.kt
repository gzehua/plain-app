package com.ismartcoding.plain

import com.ismartcoding.plain.ui.Identifiable

@PublishedApi
internal var buildTypeValue: String = ""

@PublishedApi
internal var buildChannelValue: String = ""

val buildType: String
    get() = buildTypeValue

val buildChannel: String
    get() = buildChannelValue

interface IData : Identifiable {
    override var id: String
}

data class IDData(override var id: String) : Identifiable

interface IMedia {
    val path: String
    val durationMs: Long
    val title: String
}
