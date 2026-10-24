package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.crypto.sha512 as sha512Bytes
import com.ismartcoding.plain.lib.extensions.toHexString
import kotlin.random.Random
import com.ismartcoding.plain.lib.crypto.ECDHKeyPair

expect fun verifyEd25519Signature(publicKey: ByteArray, data: ByteArray, signature: ByteArray): Boolean
expect fun generateECDHKeyPair(): ECDHKeyPair
expect fun computeECDHSharedKey(privateKeyEncoded: ByteArray, peerPublicKeyEncoded: ByteArray): String?
expect fun generateEd25519KeyPair(): Pair<ByteArray, ByteArray>
expect fun signEd25519(rawPrivateKey: ByteArray, data: ByteArray): ByteArray
expect fun verifyEd25519(rawPublicKey: ByteArray, data: ByteArray, signature: ByteArray): Boolean

/** SHA-512 of [input] as a lowercase hex string. */
fun sha512(input: ByteArray): String = sha512Bytes(input).toHexString()

/**
 * Generate a random alphanumeric password of length [n] using a human-readable
 * charset that excludes ambiguous characters (0/O, 1/I/l).
 */
fun randomPassword(n: Int): String {
    val characterSet = "23456789abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ"
    val random = Random.Default
    val password = StringBuilder()
    for (i in 0 until n) {
        val rIndex = random.nextInt(characterSet.length)
        password.append(characterSet[rIndex])
    }
    return password.toString()
}

expect fun chaCha20Decrypt(key: ByteArray, content: ByteArray): ByteArray?
expect fun chaCha20Encrypt(key: ByteArray, content: ByteArray): ByteArray
expect fun chaCha20Encrypt(key: ByteArray, content: String): ByteArray
