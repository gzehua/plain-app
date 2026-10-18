package com.ismartcoding.plain.app.events

import com.ismartcoding.plain.lib.ChannelEvent
import kotlinx.serialization.Serializable

sealed class HostEventData {
    data class Text(val value: String) : HostEventData()
    data class Binary(val value: ByteArray) : HostEventData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Binary

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }
}

class HostEvent(
    val type: EventType,
    val data: HostEventData,
) : ChannelEvent()
{
    constructor(type: EventType, data: String) : this(type, HostEventData.Text(data))
    constructor(type: EventType, data: ByteArray) : this(type, HostEventData.Binary(data))
}

enum class EventType {
    BACKGROUND_EXECUTION_GRANTED,
    BACKGROUND_EXECUTION_REVOKED,
    EXECUTION_STATE_CHANGED,
    PAIRING_REQUEST_REPORTED,
    PAIRING_RESPONSE_REPORTED,
    PAIRING_CANCEL_REPORTED,
    NOTIFICATION_REPORTED,
    IMAGE_CATALOG_CHANGED,
    NETWORK_INTERFACES_CHANGED,
    NETWORK_CHANGED,
    BLE_ADVERTISEMENT_REPORTED,
    IMAGE_INDEX_UPDATED,
    WEB_LOGIN_COMPLETED,
    WEB_SOCKET_REGISTERED,
    AUDIO_PROGRESS_REPORTED,
    AUDIO_STARTED_REPORTED,
    AUDIO_LIBRARY_UPDATED,
    MEDIA_CATALOG_CHANGED,
    MEDIA_LIBRARY_UPDATED,
    MEDIA_SCAN_PROGRESS,
    AUDIO_COMPLETED_REPORTED,
    MESSAGE_CREATED,
    MESSAGE_DELETED,
    MESSAGE_UPDATED,
    FEEDS_FETCHED,
    FEED_SYNC_UPDATED,
    APP_UPDATE_DOWNLOAD_UPDATED,
    CONTENT_CHANGED,
    SCREEN_MIRRORING,
    NOTIFICATION_CREATED,
    SCREEN_MIRROR_VIDEO,
    SCREEN_MIRROR_VIDEO_CODEC,
    SCREEN_MIRROR_AUDIO,
    NOTIFICATION_UPDATED,
    NOTIFICATION_DELETED,
    NOTIFICATION_REFRESHED,
    POMODORO_ACTION,
    POMODORO_SETTINGS_UPDATE,
    SCREEN_MIRROR_AUDIO_GRANTED,
    BOOKMARK_UPDATED,
    DOWNLOAD_PROGRESS,
    MMS_SENT,
    CHANNELS_UPDATED,
    IMAGE_SEARCH_UPDATED,
    PEER_UPDATED,
    PEER_STATUS_UPDATED,
    DEVICE_NAME_UPDATED,
    PAIRING_REQUEST_RECEIVED,
    PAIRING_SUCCESS,
    PAIRING_FAILED,
    PAIRING_CANCELED,
    PAIRING_STARTED,
    PAIRING_STATE_CHANGED,
    NEARBY_DEVICE_FOUND,
    NEARBY_DISCOVERY_STARTED,
    NEARBY_DISCOVERY_STOPPED,
    IMAGE_EDITOR_UPDATE,
    SMS_PROVIDER_CHANGED,
    SMS_SEND_RESULT,
    SMS_PART_SENT_REPORTED,
    MMS_SEND_RESULT,
    UPLOAD_MERGE_RESULT,
    CLIPBOARD_CHANGED,
    CLIPBOARD_REPORTED,
    // Payload: JSON array of granted permission names — a state snapshot
    // identical to the `app.permissions` GraphQL field (not a change set).
    PERMISSIONS_UPDATED,
    MDNS_UPDATED,
    PEER_CONNECTIONS_UPDATED,
    PEER_TRANSPORT_UPDATED,
    SHARED_FOLDER_DOWNLOAD_UPDATED,
    DLNA_RENDERER_UPDATED,
    ONLINE_CLIENTS_UPDATED,
    WEB_REQUEST_RECEIVED,
    WEB_LOGIN_REQUESTED,
    HTTP_SERVER_STARTED,
    HTTP_SERVER_FAILED,
    MEDIA_ACTION_FINISHED,
    DLNA_SENDER_UPDATED,
    IMAGE_MODELS_UPDATED,
    PREFS_UPDATED,
    NEARBY_DEVICES_UPDATED,

}


@Serializable
data class PeerStatusData(
    val id: String,
    val online: Boolean,
)

@Serializable
data class ClipboardReportData(
    val text: String,
    val sensitive: Boolean,
)

// Payload of UPLOAD_MERGE_RESULT — mirrors the desktop local server contract
// (docs/upload-async-merge.md in plain-desktop).
@Serializable
data class UploadMergeResultData(
    val fileId: String,
    val ok: Boolean,
    val value: String? = null,
    val mergedSize: Long? = null,
    val error: String? = null,
)

@Serializable
data class SmsProviderChangedData(
    val uris: List<String>,
)

@Serializable
data class SmsSendResultData(
    val requestId: String?,
    val success: Boolean,
    val resultCode: Int,
)

object SendResultCodes {
    const val TIMEOUT = -1000
    const val CANCELLED = -1001
}

@Serializable
data class MmsSendResultData(
    val pendingId: String,
    val success: Boolean,
    val resultCode: Int,
) {
    companion object {
        fun success(pendingId: String) = MmsSendResultData(
            pendingId = pendingId,
            success = true,
            resultCode = 0,
        )

        fun timeout(pendingId: String) = MmsSendResultData(
            pendingId = pendingId,
            success = false,
            resultCode = SendResultCodes.TIMEOUT,
        )

        fun cancelled(pendingId: String) = MmsSendResultData(
            pendingId = pendingId,
            success = false,
            resultCode = SendResultCodes.CANCELLED,
        )
    }
}
