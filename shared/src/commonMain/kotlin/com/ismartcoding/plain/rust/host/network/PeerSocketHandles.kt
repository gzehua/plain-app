package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.platform.network.PeerByteSocket
import com.ismartcoding.plain.platform.system.PlatformLock

internal class PeerSocketHandles(
    private val opener: suspend (String) -> PeerByteSocket,
    private val capacity: Int = 16,
) {
    private class Slot(val peerId: String) {
        var socket: PeerByteSocket? = null
        var closed = false
    }

    private val lock = PlatformLock()
    private val slots = mutableMapOf<String, Slot>()

    suspend fun open(token: String, peerId: String) {
        val slot = Slot(peerId)
        lock.withLock {
            check(slots.size < capacity && !slots.containsKey(token)) { "Socket handle unavailable" }
            slots[token] = slot
        }
        var acquired: PeerByteSocket? = null
        try {
            val socket = opener(peerId)
            acquired = socket
            val accepted = lock.withLock {
                if (slot.closed || slots[token] !== slot) false else {
                    slot.socket = socket
                    true
                }
            }
            check(accepted) { "Socket handle canceled" }
            acquired = null
        } catch (error: Throwable) {
            lock.withLock {
                slot.closed = true
                if (slots[token] === slot) slots.remove(token)
            }
            try { acquired?.close() } catch (cleanup: Throwable) { error.addSuppressed(cleanup) }
            throw error
        }
    }

    fun socket(token: String): PeerByteSocket = lock.withLock {
        checkNotNull(slots[token]?.takeUnless { it.closed }?.socket) { "Socket handle unavailable" }
    }

    fun close(token: String) = closeMatching { candidate, _ -> candidate == token }

    fun closePeer(peerId: String) = closeMatching { _, slot -> slot.peerId == peerId }

    fun closeAll() = closeMatching { _, _ -> true }

    private fun closeMatching(matches: (String, Slot) -> Boolean) {
        val sockets = lock.withLock {
            val selected = slots.filter { (token, slot) -> matches(token, slot) }
            selected.forEach { (token, slot) ->
                slot.closed = true
                slots.remove(token)
            }
            selected.values.mapNotNull { it.socket }
        }
        var failure: Throwable? = null
        sockets.forEach { socket ->
            try { socket.close() } catch (error: Throwable) {
                failure?.addSuppressed(error) ?: run { failure = error }
            }
        }
        failure?.let { throw it }
    }
}
