package com.ismartcoding.plain.ui.feeds

enum class FeedSyncErrorCode {
    NONE,
    NO_NETWORK,
    DNS,
    TIMEOUT,
    SERVER,
    SSL,
    PARSE,
    UNKNOWN;

    companion object {
        fun fromString(code: String): FeedSyncErrorCode? =
            if (code.isEmpty()) null else entries.firstOrNull { it.name == code }
    }
}
