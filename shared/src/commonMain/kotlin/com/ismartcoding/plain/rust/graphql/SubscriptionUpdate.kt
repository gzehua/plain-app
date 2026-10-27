package com.ismartcoding.plain.rust.graphql

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

sealed interface SubscriptionUpdate<out T> {
    data class Data<T>(val value: T) : SubscriptionUpdate<T>
    data object Attached : SubscriptionUpdate<Nothing>
    data class Failed(val error: Throwable) : SubscriptionUpdate<Nothing>
}

fun Flow<SubscriptionUpdate<*>>.changes(): Flow<Unit> = transform { update ->
    if (update !is SubscriptionUpdate.Failed) emit(Unit)
}
