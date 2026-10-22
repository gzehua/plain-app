package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/** A user preference whose writes are submitted to Rust and whose state is
 * published only from committed snapshots received through the Host API. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class Pref<T> internal constructor(
    internal val key: String,
    private val serializer: KSerializer<T>,
    default: T,
) : MutableStateFlow<T> {
    private val state = MutableStateFlow(default)

    override var value: T
        get() = state.value
        set(next) {
            PrefStore.write(key, JsonHelper.json.encodeToJsonElement(serializer, next))
        }

    override val replayCache: List<T> get() = listOf(state.value)

    override suspend fun collect(collector: FlowCollector<T>): Nothing = state.collect(collector)

    override suspend fun emit(value: T) {
        this.value = value
    }

    override fun tryEmit(value: T): Boolean {
        this.value = value
        return true
    }

    override fun compareAndSet(expect: T, update: T): Boolean {
        if (state.value != expect) return false
        value = update
        return true
    }

    override val subscriptionCount: StateFlow<Int> get() = state.subscriptionCount

    override fun resetReplayCache() = state.resetReplayCache()

    /** Adopts a value that already came from the store; never writes back. */
    internal fun restore(next: T) {
        state.value = next
    }

    /** Decodes a stored entry, falling back to [default] when it is absent or unreadable. */
    internal fun decode(element: JsonElement?, default: T): T =
        element?.let { runCatching { JsonHelper.json.decodeFromJsonElement(serializer, it) }.getOrNull() } ?: default
}

/** Declares a preference whose stored form is [T]'s own JSON encoding. */
internal inline fun <reified T> userPref(key: String, default: T): Pref<T> =
    PrefStore.register(Pref(key, serializer(), default))

/**
 * Declares a preference stored as a small integer code — `ordinal`, or an
 * explicit `value` for enums that carry one.
 *
 * Kotlin gets the enum, Rust keeps the integer: the storage format is
 * unchanged, so existing files and the remote clients keep working while call
 * sites stop juggling `entries[index]` and `.ordinal`.
 */
internal inline fun <reified T : Enum<T>> userEnumPref(
    key: String,
    default: T,
    crossinline code: (T) -> Int = { it.ordinal },
): Pref<T> = PrefStore.register(Pref(key, enumCodeSerializer(default, code), default))

/**
 * Reads and writes an enum as its integer code. An unknown stored code falls
 * back to [default] rather than throwing, so a value written by a newer build
 * cannot stop the app from starting.
 */
internal inline fun <reified T : Enum<T>> enumCodeSerializer(
    default: T,
    crossinline code: (T) -> Int,
): KSerializer<T> = object : KSerializer<T> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("PrefEnumCode", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeInt(code(value))

    override fun deserialize(decoder: Decoder): T {
        val stored = decoder.decodeInt()
        return enumValues<T>().firstOrNull { code(it) == stored } ?: default
    }
}