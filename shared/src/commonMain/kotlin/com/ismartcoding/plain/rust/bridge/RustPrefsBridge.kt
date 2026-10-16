package com.ismartcoding.plain.rust.bridge

internal expect object RustPrefsBridge {
    fun open(dataDir: String, cacheDir: String)

    /**
     * The whole user-settings store as one JSON object, with Rust's defaults
     * already merged. One call seeds every App-side settings flow, so startup
     * needs no per-key round trip and Kotlin never becomes a second source of
     * defaults. Keys are the serde camelCase names — the GraphQL field names.
     */
    fun userSnapshot(): String

}
