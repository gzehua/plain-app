package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.platform.system.PackageType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Package(
    override var id: String,
    var name: String,
    var type: PackageType,
    var version: String,
    var path: String,
    var size: Long,
    var certs: List<Certificate> = emptyList(),
    var installedAt: Instant,
    var updatedAt: Instant,
) : com.ismartcoding.plain.db.IData
