package com.package1.shopcook.model

import kotlinx.serialization.Serializable

@Serializable
data class GroceryItem(
    val id: String,
    val name: String,
    val category: GroceryCategory,
    val amount: Double,
    val unit: String,
    val estimatedPrice: Double,
    val isChecked: Boolean = false,
    val recipeId: String? = null,
    val recipeName: String? = null,
    val isAlreadyInKitchen: Boolean = false,
    val originalPrice: Double = estimatedPrice
)

enum class GroceryCategory(val displayName: String) {
    PRODUCE("Produce"),
    MEAT("Meat & Seafood"),
    DAIRY("Dairy & Eggs"),
    PANTRY("Pantry & Dry Goods"),
    FROZEN("Frozen Foods"),
    BAKERY("Bakery"),
    SNACKS("Snacks & Beverages"),
    OTHER("Other")
}
