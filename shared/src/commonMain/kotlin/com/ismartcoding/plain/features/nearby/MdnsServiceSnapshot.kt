package com.ismartcoding.plain.features.nearby

data class MdnsServiceSnapshot(
    val serviceType: String,
    val instanceName: String,
    val instanceFqdn: String,
    val hostname: String,
    val port: Int,
    val txtRecords: List<String>,
    val ips: List<String>,
    val complete: Boolean,
)
