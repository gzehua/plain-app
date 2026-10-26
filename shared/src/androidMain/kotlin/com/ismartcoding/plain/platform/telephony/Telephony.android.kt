package com.ismartcoding.plain.platform.telephony

import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted

import com.ismartcoding.plain.models.Sim
import android.annotation.SuppressLint
import com.ismartcoding.plain.subscriptionManager

@SuppressLint("MissingPermission")
fun getSims(): List<Sim> {
    if (!Permission.READ_PHONE_STATE.isGranted()) return emptyList()
    val subs = subscriptionManager.activeSubscriptionInfoList ?: return emptyList()
    return subs.map { info ->
        Sim(
            id = info.subscriptionId.toString(),
            label = info.displayName?.toString() ?: info.carrierName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
            number = info.number ?: "",
            subscriptionId = info.subscriptionId,
        )
    }
}
