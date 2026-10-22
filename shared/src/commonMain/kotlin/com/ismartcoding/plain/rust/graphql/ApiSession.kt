package com.ismartcoding.plain.rust.graphql

class ApiSession(
    val baseUrl: String,
    val clientId: String,
    private val token: String,
) {
    internal fun headers(): Map<String, String> = mapOf(
        "c-id" to clientId,
        "Authorization" to "Bearer $token",
    )
}
