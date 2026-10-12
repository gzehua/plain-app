package com.ismartcoding.plain.rust.request

sealed interface SessionAuth {
    /** Paired device: `c-id` header, body XChaCha-encrypted with the session key. */
    data class Paired(val sessionKey: ByteArray) : SessionAuth

    /** Web console password login (`CUSTOM` session): Bearer header, plaintext body. */
    data class Bearer(val token: String) : SessionAuth
}

class ContentApiSession(
    val baseUrl: String,
    val clientId: String,
    private val auth: SessionAuth,
) {
    internal fun headers(): Map<String, String> = buildMap {
        put("c-id", clientId)
        when (auth) {
            is SessionAuth.Bearer -> put("Authorization", "Bearer ${auth.token}")
            is SessionAuth.Paired -> Unit
        }
    }

    internal val sessionKey: ByteArray?
        get() = (auth as? SessionAuth.Paired)?.sessionKey
}