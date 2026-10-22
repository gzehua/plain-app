package com.ismartcoding.plain.rust.http

import com.ismartcoding.plain.rust.PlainRust

import com.ismartcoding.plain.lib.JsonHelper

import kotlinx.serialization.json.*

object RustTlsCertificate {
    suspend fun signature(): ByteArray = signatureBytes(PlainRust.mainQuery("tlsCertificateSignature").getValue("tlsCertificateSignature"))
    suspend fun regenerate(): ByteArray = PlainRust.regenerateTlsCertificate().map { it.toByte() }.toByteArray()
    suspend fun importPem(certificate: String, key: String): ByteArray = PlainRust.importTlsCertificatePem(certificate, key).map { it.toByte() }.toByteArray()
    suspend fun importPkcs12(bytes: ByteArray, password: String): ByteArray = PlainRust.importTlsCertificatePkcs12(bytes, password).map { it.toByte() }.toByteArray()
    private fun signatureBytes(value: JsonElement): ByteArray = JsonHelper.jsonDecodeFromElement<List<Int>>(value).map { it.toByte() }.toByteArray()
}
