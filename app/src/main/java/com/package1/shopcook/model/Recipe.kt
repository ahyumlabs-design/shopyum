package com.package1.shopcook.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val cookTimeMinutes: Int,
    val prepTimeMinutes: Int,
    val servings: Int,
    val pricePerServing: Double,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val cleanupLevel: CleanupLevel = CleanupLevel.LOW,
    val category: String,
    val cuisine: String = "American",
    val tags: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val calories: Int? = null,
    val proteinGrams: Int? = null,
    val carbsGrams: Int? = null,
    val fatGrams: Int? = null,
    val rating: Double = 4.8,
    val isFavorite: Boolean = false
)

enum class RecipeDifficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Moderate"),
    HARD("Hard")
}

enum class CleanupLevel(val label: String, val description: String) {
    LOW("Minimal", "1 pot / pan"),
    MEDIUM("Moderate", "2-3 dishes"),
    HIGH("Full", "Multiple pots & pans")
}

@Serializable
data class RecipeIngredient(
    val id: String,
    val name: String,
    val amount: Double,
    val unit: String,
    val isPantryStaple: Boolean = false,
    val category: GroceryCategory = GroceryCategory.PANTRY,
    val estimatedPrice: Double = 0.0
)
