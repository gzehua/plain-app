package com.ismartcoding.plain.features.contacts

import com.ismartcoding.plain.features.telephony.EmailType
import com.ismartcoding.plain.features.telephony.EventType
import com.ismartcoding.plain.features.telephony.ImProtocol
import com.ismartcoding.plain.features.telephony.PhoneType
import com.ismartcoding.plain.features.telephony.PostalType
import com.ismartcoding.plain.features.telephony.WebsiteType
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.util.ID

@Serializable
data class ContactInput(
    var prefix: String,
    var firstName: String,
    var middleName: String,
    var lastName: String,
    var suffix: String,
    var nickname: String,
    var phoneNumbers: List<ContactPhoneInput>,
    var emails: List<ContactEmailInput>,
    var addresses: List<ContactAddressInput>,
    var events: List<ContactEventInput>,
    var source: String,
    var starred: Boolean,
    var notes: String,
    var groupIds: List<ID>,
    var organization: OrganizationInput?,
    var websites: List<ContactWebsiteInput>,
    var ims: List<ContactImInput>,
)
