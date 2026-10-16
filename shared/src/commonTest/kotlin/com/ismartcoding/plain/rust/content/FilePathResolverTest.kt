package com.ismartcoding.plain.rust.content

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FilePathResolverTest {
    @Test
    fun batchesAndDeduplicatesAliasesWithoutResolvingOrdinaryPaths() = runTest {
        val requests = mutableListOf<List<String>>()
        val resolver = FilePathResolver(backgroundScope, { paths -> requests.add(paths); paths.map { "/resolved/$it" } })
        val result = async { resolver.resolveAll(listOf("app://image", "fid:first", "app://image", "/ordinary", "https://example.invalid/image")) }
        runCurrent()
        assertEquals(listOf(listOf("app://image", "fid:first")), requests)
        assertEquals(listOf("/resolved/app://image", "/resolved/fid:first", "/resolved/app://image", "/ordinary", "https://example.invalid/image"), result.await())
        assertEquals("/resolved/app://image", resolver.resolve("app://image"))
        assertEquals(1, requests.size)
    }

    @Test
    fun batchesAreBoundedAndSuccessfulCacheEntriesAreEvictedByUse() = runTest {
        val requests = mutableListOf<List<String>>()
        val resolver = FilePathResolver(backgroundScope, { paths -> requests.add(paths); paths.map { "/$it" } }, maxEntries = 2, batchSize = 2)
        val result = async { resolver.resolveAll(listOf("fid:a", "fid:b", "fid:c")) }
        runCurrent()
        assertEquals(listOf(listOf("fid:a", "fid:b"), listOf("fid:c")), requests)
        assertEquals(listOf("/fid:a", "/fid:b", "/fid:c"), result.await())
        assertEquals("/fid:b", resolver.resolve("fid:b"))
        val evicted = async { resolver.resolve("fid:a") }
        runCurrent()
        assertEquals("/fid:a", evicted.await())
        assertEquals(listOf("fid:a"), requests.last())
    }

    @Test
    fun cancellingOneWaiterDoesNotCancelTheSharedLookup() = runTest {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var requests = 0
        val resolver = FilePathResolver(backgroundScope, { paths -> requests++; entered.complete(Unit); release.await(); paths.map { "/resolved" } })
        val first = async { resolver.resolve("fid:shared") }
        val second = async { resolver.resolve("fid:shared") }
        runCurrent()
        entered.await()
        first.cancelAndJoin()
        release.complete(Unit)
        runCurrent()
        assertEquals("/resolved", second.await())
        assertEquals(1, requests)
    }

    @Test
    fun missingResultsAndCancelledRequestsDoNotPoisonLaterBatches() = runTest {
        var requests = 0
        val resolver = FilePathResolver(backgroundScope, { paths ->
            requests++
            when (requests) {
                1 -> throw CancellationException("request cancelled")
                2 -> paths.map { null }
                else -> paths.map { "/found" }
            }
        })
        val first = async { runCatching { resolver.resolve("fid:retry") } }
        runCurrent()
        assertIs<CancellationException>(first.await().exceptionOrNull())
        val second = async { resolver.resolve("fid:retry") }
        runCurrent()
        assertEquals("", second.await())
        val third = async { resolver.resolve("fid:retry") }
        runCurrent()
        assertEquals("/found", third.await())
        assertEquals(3, requests)
    }
}
