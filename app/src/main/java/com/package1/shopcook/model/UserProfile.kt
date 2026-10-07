package com.package1.shopcook.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String = "Alex Chen",
    val email: String = "alex.cooks@example.com",
    val monthlyBudget: Double = 350.0,
    val currentMonthSpent: Double = 182.40,
    val monthlySavingsGoal: Double = 120.0,
    val mealsCookedThisMonth: Int = 18,
    val dietaryPreferences: List<String> = listOf("High Protein", "Quick Prep", "Budget Conscious"),
    val householdSize: Int = 2,
    val userTier: UserTier = UserTier.FREE,
    val groceryBudget: Double = 90.0,
    val servingsCount: Int = 2,
    val activeCookingDays: List<String> = listOf("M", "T", "W", "T", "F"),
    val selectedDietaryFilters: Set<String> = emptySet(),
    val isCheapestWeek: Boolean = false,
    val selectedStore: String = "Trader Joe's",
    val zipCode: String = "90210",
    val excludedIngredients: List<String> = emptyList(),
    val shareCode: String = "YUM-8924",
    val joinedShareCode: String? = null,
    val selectedWeek: String = "This week",
    val cookingStreakWeeks: Int = 4,
    val moneySavedThisMonth: Double = 340.0
) {
    val isProUser: Boolean
        get() = userTier != UserTier.FREE

    val billingState: BillingState
        get() = when (userTier) {
            UserTier.FREE -> BillingState.FREE
            UserTier.PRO -> BillingState.PRO
            UserTier.LIFETIME -> BillingState.LIFETIME
        }

    val isAdFree: Boolean
        get() = billingState.isAdFree
}
