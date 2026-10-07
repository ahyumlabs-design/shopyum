package com.package1.shopcook.model

import kotlinx.serialization.Serializable

@Serializable
data class MealPlanItem(
    val id: String,
    val dayOfWeek: String,
    val mealType: MealType,
    val recipe: Recipe,
    val servings: Int = 2,
    val isCooked: Boolean = false,
    val dateLabel: String = ""
)

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}
