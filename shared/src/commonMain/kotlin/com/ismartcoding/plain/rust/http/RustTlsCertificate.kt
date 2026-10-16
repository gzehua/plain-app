package com.ismartcoding.plain.rust.http

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.helpers.Base64Lenient
import kotlinx.serialization.json.*

object RustTlsCertificate {
    suspend fun signature(): ByteArray = signatureBytes(PlainRust.mainQuery("tlsCertificateSignature").getValue("tlsCertificateSignature"))
    suspend fun regenerate(): ByteArray = signatureBytes(PlainRust.mainMutate("regenerateTlsCertificate").getValue("regenerateTlsCertificate"))
    suspend fun importPem(certificate: String, key: String): ByteArray = signatureBytes(PlainRust.mainMutate("importTlsCertificatePem(certificatePem: ${gql(certificate)}, privateKeyPem: ${gql(key)})").getValue("importTlsCertificatePem"))
    suspend fun importPkcs12(bytes: ByteArray, password: String): ByteArray = signatureBytes(PlainRust.mainMutate("importTlsCertificatePkcs12(data: ${gql(Base64Lenient.encode(bytes))}, password: ${gql(password)})").getValue("importTlsCertificatePkcs12"))
    private fun signatureBytes(value: JsonElement): ByteArray = value.jsonArray.map { it.jsonPrimitive.int.toByte() }.toByteArray()
}
