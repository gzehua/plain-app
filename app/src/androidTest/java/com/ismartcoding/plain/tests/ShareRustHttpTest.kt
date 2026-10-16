package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ismartcoding.plain.features.share.ShareManager
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import kotlinx.serialization.json.*
import com.ismartcoding.plain.preferences.UserPrefs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class ShareRustHttpTest {
    @Test
    fun sharesResolveFreshRustRootsAndRevokeOldTokens() = runBlocking {
        val marker = "share-rust-${UUID.randomUUID()}"
        val root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, marker).apply { mkdirs() }
        val folder = File(root,"photos").apply { mkdirs() }
        val other = File(root,"other").apply { mkdirs() }
        File(folder,"a.txt").writeText("synthetic")
        File(folder,"empty").mkdir()
        val outside = File(other,"secret.txt").apply { writeText("outside") }
        android.system.Os.symlink(outside.absolutePath, File(folder,"escape.txt").absolutePath)
        val service = UserPrefs.service.value
        var id: String? = null
        try {
            UserPrefs.service.value = true
            val share = ShareManager.createShare(marker,listOf(folder.absolutePath),true,null)
            id = share.id
            assertTrue(ShareManager.buildLink(share, "127.0.0.1").contains("/s/${share.id}#"))
            assertNotNull(ShareManager.loadAuth(share.id))
            val encrypted = ResourceCrypto.encrypt("{\"sharedId\":\"${share.id}\",\"virtualPath\":\"photos/a.txt\"}",Base64.decode(share.urlToken))
            assertEquals(File(folder,"a.txt").canonicalPath,ShareManager.resolveSharedPath(share.id,encrypted))
            assertEquals("photos/a.txt",ShareManager.browse(share.id,"photos").second.filter { !it.isDir }.single().virtualPath)
            val folderId = ResourceCrypto.encrypt("{\"sharedId\":\"${share.id}\",\"virtualPath\":\"photos\"}",Base64.decode(share.urlToken))
            val entries = PlainRust.mainQuery("shareZipEntries(id: ${gql(share.id)}, fileId: ${gql(folderId)}) { virtualPath realPath isDir }")
                .getValue("shareZipEntries").jsonArray
            assertEquals(listOf("a.txt", "empty/"), entries.map { it.jsonObject.getValue("virtualPath").jsonPrimitive.content })
            assertTrue(entries.single { it.jsonObject.getValue("isDir").jsonPrimitive.boolean }.jsonObject.getValue("realPath").jsonPrimitive.content.endsWith("/empty"))
            assertNull(ShareManager.resolveVirtualPath(share,"photos/../other"))
            UserPrefs.service.value = false
            assertNull(ShareManager.loadAuth(share.id))
            assertNull(ShareManager.resolveSharedPath(share.id,encrypted))
            UserPrefs.service.value = true
            ShareManager.updateShare(share.id,marker,null,listOf(other.absolutePath))
            assertNull(ShareManager.resolveSharedPath(share.id,encrypted))
            ShareManager.updateShare(share.id,marker,Instant.parse("2000-01-01T00:00:00Z"))
            assertNull(ShareManager.loadAuth(share.id))
            ShareManager.updateShare(share.id,marker,null)
            assertNotNull(ShareManager.loadAuth(share.id))
            ShareManager.deleteShare(share.id)
            assertNull(ShareManager.loadAuth(share.id))
            assertNull(ShareManager.getShare(share.id))
        } finally {
            id?.let { ShareManager.deleteShare(it) }
            UserPrefs.service.value = service
            root.deleteRecursively()
        }
    }
}
