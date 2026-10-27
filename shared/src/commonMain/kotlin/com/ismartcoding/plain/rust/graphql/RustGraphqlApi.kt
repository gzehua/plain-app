package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustNativeBindings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object RustGraphqlApi : GraphqlClient(::executeNativeGraphql)

private suspend fun executeNativeGraphql(request: GraphqlRequest): GraphqlResponse =
    withContext(Dispatchers.IO) {
        JsonHelper.jsonDecode(RustNativeBindings.graphql(JsonHelper.jsonEncode(request)))
    }
