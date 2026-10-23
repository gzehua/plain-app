package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.testing.CryptoFixtures
import com.ismartcoding.plain.lib.crypto.ECDHKeyPair

actual fun verifyEd25519Signature(publicKey: ByteArray, data: ByteArray, signature: ByteArray): Boolean =
    CryptoFixtures.verifyEd25519Signature(publicKey, data, signature)
actual fun generateECDHKeyPair(): ECDHKeyPair =
    CryptoFixtures.generateECDHKeyPair()
actual fun computeECDHSharedKey(privateKeyEncoded: ByteArray, peerPublicKeyEncoded: ByteArray): String? =
    CryptoFixtures.computeECDHSharedKey(privateKeyEncoded, peerPublicKeyEncoded)
actual fun generateEd25519KeyPair(): Pair<ByteArray, ByteArray> = CryptoFixtures.generateEd25519KeyPair()
actual fun signEd25519(rawPrivateKey: ByteArray, data: ByteArray): ByteArray =
    CryptoFixtures.signEd25519(rawPrivateKey, data)
actual fun verifyEd25519(rawPublicKey: ByteArray, data: ByteArray, signature: ByteArray): Boolean =
    CryptoFixtures.verifyEd25519(rawPublicKey, data, signature)

actual fun chaCha20Decrypt(key: ByteArray, content: ByteArray): ByteArray? =
    CryptoFixtures.chaCha20Decrypt(key, content)
actual fun chaCha20Encrypt(key: ByteArray, content: ByteArray): ByteArray =
    CryptoFixtures.chaCha20Encrypt(key, content)
actual fun chaCha20Encrypt(key: ByteArray, content: String): ByteArray =
    CryptoFixtures.chaCha20Encrypt(key, content)
