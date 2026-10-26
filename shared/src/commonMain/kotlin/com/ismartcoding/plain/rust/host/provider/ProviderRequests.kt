package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class ProviderPlanRequest(val plan: ProviderPlan)
