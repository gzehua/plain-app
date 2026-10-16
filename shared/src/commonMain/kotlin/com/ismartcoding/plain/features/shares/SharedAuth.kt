package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.api.graphql.models.ShareRecord

data class SharedAuth(val share: ShareRecord, val token: ByteArray)
