package com.package1.shopcook.model

import kotlinx.serialization.Serializable

enum class ReminderType {
    THAW,
    SHOPPING_DAY,
    EXPIRATION
}

@Serializable
data class SmartReminder(
    val id: String,
    val type: ReminderType,
    val title: String,
    val description: String,
    val iconEmoji: String
)
