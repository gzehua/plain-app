package com.ismartcoding.plain.features.telephony.sms

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Sms
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object RustSmsQuery {
    suspend fun search(query: String, limit: Int, offset: Int): List<Sms> =
        RustGraphqlApi.execute(Query.sms(query, offset, limit))

    suspend fun count(query: String): Int =
        RustGraphqlApi.execute(Query.smsCount(query))
}
