package com.ismartcoding.plain.features.share

import com.ismartcoding.plain.graphql.models.ShareRecord

data class SharedAuth(val share: ShareRecord, val token: ByteArray)
