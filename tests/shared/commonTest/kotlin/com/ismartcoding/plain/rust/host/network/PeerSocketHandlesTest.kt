package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.platform.network.PeerByteSocket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PeerSocketHandlesTest {
    private class Socket(private val closeError: Throwable? = null) : PeerByteSocket {
        var closes = 0
        override suspend fun read(length: Int): ByteArray = byteArrayOf(1)
        override suspend fun write(bytes: ByteArray) {}
        override fun close() {
            closes++
            closeError?.let { throw it }
        }
    }

    @Test fun revoking_one_peer_closes_only_its_handles_once() = runTest {
        val sockets = List(3) { Socket() }
        var opens = 0
        val handles = PeerSocketHandles({ sockets[opens++] })
        handles.open("first", "peer-a")
        handles.open("second", "peer-b")
        handles.open("third", "peer-a")
        handles.closePeer("peer-a")
        assertEquals(listOf(1, 0, 1), sockets.map { it.closes })
        assertSame(sockets[1], handles.socket("second"))
        assertFailsWith<IllegalStateException> { handles.socket("first") }
        handles.closePeer("peer-a")
        handles.closeAll()
        handles.closeAll()
        assertEquals(listOf(1, 1, 1), sockets.map { it.closes })
    }

    @Test fun a_late_open_closes_its_socket_without_closing_a_reused_token() = runTest {
        val release = CompletableDeferred<Unit>()
        val late = Socket()
        val replacement = Socket()
        var opens = 0
        val handles = PeerSocketHandles({
            if (opens++ == 0) { release.await(); late } else replacement
        })
        val pending = async { runCatching { handles.open("same", "old-peer") } }
        runCurrent()
        handles.closePeer("old-peer")
        handles.open("same", "new-peer")
        release.complete(Unit)
        assertTrue(pending.await().isFailure)
        assertEquals(1, late.closes)
        assertEquals(0, replacement.closes)
        assertSame(replacement, handles.socket("same"))
        handles.closeAll()
    }

    @Test fun a_failed_old_open_cannot_remove_a_reused_token() = runTest {
        val release = CompletableDeferred<Unit>()
        val replacement = Socket()
        var opens = 0
        val handles = PeerSocketHandles({
            if (opens++ == 0) { release.await(); error("SDK connect failed") } else replacement
        })
        val pending = async { runCatching { handles.open("same", "old-peer") } }
        runCurrent()
        handles.close("same")
        handles.open("same", "new-peer")
        release.complete(Unit)
        assertEquals("SDK connect failed", pending.await().exceptionOrNull()?.message)
        assertEquals(0, replacement.closes)
        assertSame(replacement, handles.socket("same"))
        handles.closeAll()
    }

    @Test fun capacity_and_duplicate_tokens_are_checked_before_sdk_open_including_pending_handles() = runTest {
        val release = CompletableDeferred<Unit>()
        val sockets = List(3) { Socket() }
        var opens = 0
        val handles = PeerSocketHandles({
            val index = opens++
            if (index == 1) release.await()
            sockets[index]
        }, capacity = 2)
        handles.open("first", "peer-a")
        val pending = async { runCatching { handles.open("pending", "peer-b") } }
        runCurrent()
        assertFailsWith<IllegalStateException> { handles.open("first", "peer-c") }
        assertFailsWith<IllegalStateException> { handles.open("third", "peer-c") }
        assertEquals(2, opens)
        handles.close("pending")
        handles.open("third", "peer-c")
        release.complete(Unit)
        assertTrue(pending.await().isFailure)
        assertEquals(1, sockets[1].closes)
        assertSame(sockets[2], handles.socket("third"))
        handles.closeAll()
    }

    @Test fun cancellation_releases_the_pending_reservation_for_another_open() = runTest {
        val release = CompletableDeferred<Unit>()
        val socket = Socket()
        var opens = 0
        val handles = PeerSocketHandles({
            if (opens++ == 0) release.await()
            socket
        }, capacity = 1)
        val pending = async { handles.open("pending", "peer-a") }
        runCurrent()
        pending.cancelAndJoin()
        handles.open("replacement", "peer-b")
        assertSame(socket, handles.socket("replacement"))
        assertEquals(2, opens)
        handles.closeAll()
        assertEquals(1, socket.closes)
    }

    @Test fun close_failure_still_releases_every_handle_and_preserves_other_failures() = runTest {
        val firstError = IllegalStateException("first close failed")
        val secondError = IllegalArgumentException("second close failed")
        val first = Socket(firstError)
        val second = Socket(secondError)
        val replacement = Socket()
        val sockets = listOf(first, second, replacement)
        var opens = 0
        val handles = PeerSocketHandles({ sockets[opens++] }, capacity = 2)
        handles.open("first", "peer")
        handles.open("second", "peer")
        val error = assertFailsWith<IllegalStateException> { handles.closeAll() }
        assertSame(firstError, error)
        assertEquals(listOf(secondError), error.suppressedExceptions)
        assertEquals(1, first.closes)
        assertEquals(1, second.closes)
        handles.closeAll()
        handles.open("replacement", "peer")
        assertSame(replacement, handles.socket("replacement"))
        handles.closeAll()
    }
}
