package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.setMdnsMulticastPermission
import com.ismartcoding.plain.platform.system.PlatformLock
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.json.*

object MdnsMulticastHost {
    private val lock = PlatformLock()
    private val active = linkedSetOf<String>()
    private val closed = linkedSetOf<String>()

    internal fun handle(params: JsonObject): HostResult = lock.withLock {
        val request = JsonHelper.jsonDecodeFromElement<MulticastPermissionRequest>(params)
        val lease = request.lease
        require(lease.isNotEmpty() && lease.length <= 128)
        val acquire = request.acquire
        if (acquire) {
            if (lease in closed) return@withLock hostResult(false)
            val acquired = setMdnsMulticastPermission(true)
            if (acquired) active.add(lease)
            hostResult(acquired)
        } else {
            closed.add(lease)
            while (closed.size > 128) closed.remove(closed.first())
            if (!active.remove(lease) || active.isNotEmpty()) return@withLock hostResult(true)
            val released = setMdnsMulticastPermission(false)
            if (!released) active.add(lease)
            hostResult(released)
        }
    }
    fun disconnect() = lock.withLock {
        setMdnsMulticastPermission(false)
        active.clear()
    }
}
