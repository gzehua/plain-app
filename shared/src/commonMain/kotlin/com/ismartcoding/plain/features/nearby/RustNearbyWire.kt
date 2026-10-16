package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.features.nearby.NearbyMessageType
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.RustBleBridge
import com.ismartcoding.plain.rust.request.gql
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

object RustNearbyWire {
    fun encode(type: NearbyMessageType, payload: String): String {
        val message = buildJsonObject {
            put("kind", type.name)
            if (type != NearbyMessageType.DISCOVER) put("payload", Json.parseToJsonElement(payload))
        }
        return checkNotNull(RustBleBridge.call(11, 0, 0, 0, 0, false, message.toString().encodeToByteArray())).decodeToString()
    }

    fun parse(body: String): Pair<NearbyMessageType, String>? = try {
        val bytes = checkNotNull(RustBleBridge.call(12, 0, 0, 0, 0, false, body.encodeToByteArray()))
        val message = Json.parseToJsonElement(bytes.decodeToString()).jsonObject
        NearbyMessageType.valueOf(message.getValue("kind").jsonPrimitive.content) to (message["payload"]?.toString() ?: "")
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { null }

    suspend fun beginScan(): String = PlainRust.mainMutate("beginBleScan").getValue("beginBleScan").jsonPrimitive.content
    suspend fun endScan(id: String) { PlainRust.mainMutate("endBleScan(scanId: ${gql(id)})") }
    suspend fun scanSeen(id: String, shortId: String) {
        PlainRust.publish(HostEvent(EventType.BLE_ADVERTISEMENT_REPORTED, buildJsonObject { put("scanId", id); put("shortId", shortId) }.toString()))
    }
}
