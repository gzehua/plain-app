package com.ismartcoding.plain.rust.request

import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.*

object RustApi {
    suspend fun query(selection: String, session: ApiSession? = null): JsonObject =
        PlainRust.mainQuery(selection, session)

    suspend fun mutate(selection: String, session: ApiSession? = null, longRunning: Boolean = false): JsonObject =
        PlainRust.mainMutate(selection, session, longRunning)

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ApiSession? = null): JsonObject =
        PlainRust.mainGraphql(document, variables, session)
}
