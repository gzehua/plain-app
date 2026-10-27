package com.ismartcoding.plain.receivers

import com.ismartcoding.plain.rust.native.RustHostReports
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.ismartcoding.plain.features.telephony.sms.SmsPartSentReport
import com.ismartcoding.plain.lib.JsonHelper

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: return
        val partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0)
        val partCount = intent.getIntExtra(EXTRA_PART_COUNT, 1)
        val broadcastResultCode = resultCode
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                val report = SmsPartSentReport(requestId, partIndex, partCount,
                    broadcastResultCode == Activity.RESULT_OK, broadcastResultCode)
                RustHostReports.smsPartSent(JsonHelper.jsonEncode(report))
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
