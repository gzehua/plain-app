package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object RemoteFiles {
    suspend fun cache(url: String, name: String = ""): String =
        RustGraphqlApi.read(Mutation.cacheRemoteFile(url, name))
}
