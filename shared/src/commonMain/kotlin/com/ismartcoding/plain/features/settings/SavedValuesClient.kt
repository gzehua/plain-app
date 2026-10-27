package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object SavedValuesClient {
    suspend inline fun <reified T> get(key: String, default: T): T =
        RustGraphqlApi.execute<T?>(Query.savedValue(key)) ?: default

    suspend inline fun <reified T> set(key: String, value: T) {
        RustGraphqlApi.execute<Unit>(Mutation.setSavedValue(key, JsonHelper.jsonEncodeToElement(value)))
    }
}
