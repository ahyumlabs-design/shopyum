package com.package1.shopcook.model

import kotlinx.serialization.Serializable

@Serializable
enum class BillingState {
    FREE,
    PRO,
    LIFETIME;

    val isAdFree: Boolean
        get() = this != FREE
}
