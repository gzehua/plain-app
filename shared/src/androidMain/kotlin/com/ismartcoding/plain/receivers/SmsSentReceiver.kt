package com.ismartcoding.plain.receivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ismartcoding.plain.lib.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: return
        val partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0)
        val partCount = intent.getIntExtra(EXTRA_PART_COUNT, 1)
        val broadcastResultCode = resultCode
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                val report = com.ismartcoding.plain.features.telephony.sms.SmsPartSentReport(requestId, partIndex, partCount,
                    broadcastResultCode == Activity.RESULT_OK, broadcastResultCode)
                com.ismartcoding.plain.rust.RustEventHandler.sendEvent(com.ismartcoding.plain.app.events.HostEvent(
                    com.ismartcoding.plain.app.events.EventType.SMS_PART_SENT_REPORTED, com.ismartcoding.plain.lib.JsonHelper.jsonEncode(report)))
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REQUEST_ID = "sms_request_id"
        const val EXTRA_PART_INDEX = "sms_part_index"
        const val EXTRA_PART_COUNT = "sms_part_count"

        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
