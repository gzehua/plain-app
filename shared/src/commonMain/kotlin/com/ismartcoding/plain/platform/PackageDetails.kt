package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.Package

data class PackageDetails(
    val item: Package,
    val sourceDir: String,
    val dataDir: String,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val versionCode: Long,
    val hasLargeHeap: Boolean,
)
