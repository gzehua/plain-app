package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.features.nearby.PeerStatus
import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant

@kotlinx.serialization.Serializable
data class PeerRecord(
    var id: String,
    @kotlinx.serialization.Required var name: String = "",
    @kotlinx.serialization.Required var ip: String = "",
    @kotlinx.serialization.Required var key: String = "",
    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("public_key") var publicKey: String = "",
    @kotlinx.serialization.Required var status: PeerStatus = PeerStatus.UNPAIRED,
    @kotlinx.serialization.Required var port: Int? = null,
    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("device_type") var deviceType: DeviceType = DeviceType.PHONE,

    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("created_at") var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required @kotlinx.serialization.SerialName("updated_at") var updatedAt: Instant = TimeHelper.now(),
    var address: PeerAddress? = null,
    var token: String = "",
) {
    fun isPaired(): Boolean = status == PeerStatus.PAIRED
    fun getIpList(): List<String> {
        return ip.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
