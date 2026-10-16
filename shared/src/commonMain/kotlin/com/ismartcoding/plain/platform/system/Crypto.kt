package com.ismartcoding.plain.platform.system

/**
 * Generate a fresh Base64-encoded 32-byte XChaCha20 key.
 */
expect fun generateChaCha20Key(): String
