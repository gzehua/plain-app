package com.ismartcoding.uicheck

import java.io.File
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Java21CollectionsTest {
    @get:Rule
    val temporary = TemporaryFolder()

    private val declarations = """
        import java.util.concurrent.LinkedBlockingDeque
        val queue = LinkedBlockingDeque<String>()
        val qualified = java.util.concurrent.LinkedBlockingDeque<String>()
        fun fixture(map: LinkedHashMap<String, String>, list: MutableList<String>) {
            queue.putFirst("safe")
            qualified.putLast("safe")
    """.trimIndent()

    @Test
    fun blockingDequeMethodsDoNotAllowJava21CallsOnOtherReceivers() {
        val calls = listOf("map.putFirst(\"key\", \"value\")", "list.removeFirst()", "queue.reversed()")
        val findings = java21CollectionCalls(declarations + "\n" + calls.joinToString("\n") + "\n}")
        assertEquals(calls, findings.map { it.substringAfter(": ") })
        assertTrue(java21CollectionCalls("$declarations\n}").isEmpty())
    }

    @Test
    fun commandRejectsInvalidSourceAndAcceptsValidSource() {
        val root = temporary.newFolder()
        val file = File(root, "CollectionFixture.kt")
        file.writeText("$declarations\nlist.removeFirst()\n}")
        val failure = assertFailsWith<IllegalStateException> { main(arrayOf("--java21", root.path)) }
        assertTrue(failure.message.orEmpty().contains("CollectionFixture.kt:"))
        assertTrue(failure.message.orEmpty().contains("list.removeFirst()"))
        file.writeText("$declarations\n}")
        main(arrayOf("--java21", root.path))
    }
}
