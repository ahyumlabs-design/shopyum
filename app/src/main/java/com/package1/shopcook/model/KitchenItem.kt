package com.package1.shopcook.model

import kotlinx.serialization.Serializable

@Serializable
data class KitchenItem(
    val id: String,
    val name: String,
    val category: GroceryCategory,
    val location: StorageLocation,
    val quantity: Double,
    val unit: String,
    val expiryDaysRemaining: Int? = null,
    val isLow: Boolean = false,
    val isLeftover: Boolean = false,
    val leftoverServings: Int? = null,
    val addedDate: String? = null,
    val isAlreadyHave: Boolean = true,
    val estimatedValue: Double = 2.50
)

enum class StorageLocation(val displayName: String) {
    PANTRY("Pantry"),
    FRIDGE("Fridge"),
    FREEZER("Freezer")
}
