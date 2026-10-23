package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
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

class Pref<T> private constructor(
    internal val key: String,
    private val serializer: KSerializer<T>,
    private val state: MutableStateFlow<T>,
) : StateFlow<T> by state {
    internal constructor(key: String, serializer: KSerializer<T>, default: T) :
        this(key, serializer, MutableStateFlow(default))

    override var value: T
        get() = state.value
        set(next) {
            Prefs.write(key, JsonHelper.json.encodeToJsonElement(serializer, next))
        }

    internal fun restoreElement(value: JsonElement) = restore(decode(value, this.value))

    internal fun restore(next: T) {
        state.value = next
    }

    internal fun decode(element: JsonElement?, default: T): T =
        element?.let { runCatching { JsonHelper.json.decodeFromJsonElement(serializer, it) }.getOrNull() } ?: default
}

internal inline fun <reified T> pref(key: String): Pref<T> =
    Prefs.register(Pref(key, serializer(), JsonHelper.json.decodeFromJsonElement(serializer<T>(), Prefs.initialValue(key))))

internal inline fun <reified T : Enum<T>> enumPref(
    key: String,
    crossinline code: (T) -> Int = { it.ordinal },
): Pref<T> {
    val defaultCode = JsonHelper.json.decodeFromJsonElement(serializer<Int>(), Prefs.initialValue(key))
    val default = enumValues<T>().first { code(it) == defaultCode }
    return Prefs.register(Pref(key, enumCodeSerializer(default, code), default))
}

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
