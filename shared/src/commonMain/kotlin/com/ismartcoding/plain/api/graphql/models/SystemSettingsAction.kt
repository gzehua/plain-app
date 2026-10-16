package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
enum class SystemSettingsAction {
    SET_PASSWORD,
    SET_PASSWORD_TYPE,
    SET_TWO_FACTOR,
    SET_ROTATE_URL_TOKEN,
    RESET_PASSWORD,
    RESET_URL_TOKEN,
    RESET_ADB_TOKEN,
    SET_API_PERMISSION,
    SET_API_PERMISSIONS,
    COMPLETE_ONBOARDING,
    SET_MDNS_HOSTNAME,
    PATCH_UPDATE,
}
