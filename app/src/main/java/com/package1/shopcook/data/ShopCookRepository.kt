package com.package1.shopcook.data

import com.package1.shopcook.model.BillingState
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.GroceryItem
import com.package1.shopcook.model.KitchenItem
import com.package1.shopcook.model.MealPlanItem
import com.package1.shopcook.model.MealType
import com.package1.shopcook.model.Recipe
import com.package1.shopcook.model.RecipeIngredient
import com.package1.shopcook.model.StorageLocation
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.model.UserTier
import com.package1.shopcook.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import java.util.UUID

class ShopCookRepository private constructor() {

    val cloudSyncManager: CloudSyncManager = CloudSyncManager.instance

    private val _recipes = MutableStateFlow(MockData.sampleRecipes)
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _kitchenItems = MutableStateFlow(MockData.sampleKitchenItems)
    val kitchenItems: StateFlow<List<KitchenItem>> = _kitchenItems.asStateFlow()

    private val _mealPlan = MutableStateFlow(MockData.sampleMealPlan)
    val mealPlan: StateFlow<List<MealPlanItem>> = _mealPlan.asStateFlow()

    private val _userProfile = MutableStateFlow(MockData.sampleUserProfile)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _groceryItems = MutableStateFlow(MockData.sampleGroceryItems)
    val groceryItems: StateFlow<List<GroceryItem>> = _groceryItems.asStateFlow()

    init {
        _recipes.update { list -> list.map { sanitizeRecipe(it) } }
        _mealPlan.update { list -> list.map { item -> item.copy(recipe = sanitizeRecipe(item.recipe)) } }

        // Run sync on background thread initially to avoid ANR on startup
        CoroutineScope(Dispatchers.IO).launch {
            _groceryItems.value = syncGroceryItemsWithKitchen(MockData.sampleGroceryItems, MockData.sampleKitchenItems)
            cloudSyncManager.triggerSync()
        }
    }

    fun setUserTier(tier: UserTier) {
        _userProfile.update { it.copy(userTier = tier) }
        cloudSyncManager.queueChange("User tier updated to $tier")
    }

    fun setBillingState(state: BillingState) {
        val tier = when (state) {
            BillingState.FREE -> UserTier.FREE
            BillingState.PRO -> UserTier.PRO
            BillingState.LIFETIME -> UserTier.LIFETIME
        }
        setUserTier(tier)
    }

    fun toggleProUser() {
        _userProfile.update {
            val nextTier = if (it.userTier == UserTier.FREE) UserTier.PRO else UserTier.FREE
            it.copy(userTier = nextTier)
        }
        cloudSyncManager.queueChange("User tier toggled")
    }

    fun setSelectedWeek(week: String) {
        _userProfile.update { it.copy(selectedWeek = week) }
        cloudSyncManager.queueChange("Selected week updated")
    }

    fun getUpcomingWeekRanges(): List<String> = DateUtils.getUpcomingWeekRanges()

    fun getWeekOptions(): List<String> = DateUtils.getWeekOptions()

    fun setServingsCount(servings: Int) {
        if (servings in 1..10) {
            _userProfile.update { it.copy(servingsCount = servings) }
            _mealPlan.update { list -> list.map { it.copy(servings = servings) } }
            cloudSyncManager.queueChange("Servings count updated to $servings")
        }
    }

    fun setGroceryBudget(budget: Double) {
        _userProfile.update { it.copy(groceryBudget = budget) }
        cloudSyncManager.queueChange("Grocery budget updated")
    }

    fun setActiveCookingDays(days: List<String>) {
        _userProfile.update { it.copy(activeCookingDays = days) }
        cloudSyncManager.queueChange("Active cooking days updated")
    }

    fun toggleDietaryFilter(filter: String) {
        _userProfile.update { profile ->
            val current = profile.selectedDietaryFilters
            val updated = if (current.contains(filter)) current - filter else current + filter
            profile.copy(selectedDietaryFilters = updated)
        }
        cloudSyncManager.queueChange("Dietary filters updated")
    }

    fun setIsCheapestWeek(cheapest: Boolean) {
        _userProfile.update { it.copy(isCheapestWeek = cheapest) }
        cloudSyncManager.queueChange("Cheapest week option updated")
    }

    fun setSelectedStore(storeName: String) {
        _userProfile.update { it.copy(selectedStore = storeName) }
        recalculateGroceryPricesForStore(storeName)
        cloudSyncManager.queueChange("Selected store updated to $storeName")
    }

    fun setZipCode(zipCode: String) {
        _userProfile.update { it.copy(zipCode = zipCode) }
        cloudSyncManager.queueChange("ZIP Code updated to $zipCode")
    }

    fun addExcludedIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isNotEmpty()) {
            _userProfile.update { profile ->
                if (profile.excludedIngredients.none { it.equals(trimmed, ignoreCase = true) }) {
                    profile.copy(excludedIngredients = profile.excludedIngredients + trimmed)
                } else profile
            }
            filterGroceryItemsForExclusions()
            cloudSyncManager.queueChange("Excluded ingredient added: $trimmed")
        }
    }

    fun removeExcludedIngredient(ingredient: String) {
        _userProfile.update { profile ->
            profile.copy(excludedIngredients = profile.excludedIngredients.filterNot { it.equals(ingredient, ignoreCase = true) })
        }
        cloudSyncManager.queueChange("Excluded ingredient removed: $ingredient")
    }

    private fun filterGroceryItemsForExclusions() {
        val exclusions = _userProfile.value.excludedIngredients.map { it.lowercase() }
        if (exclusions.isNotEmpty()) {
            _groceryItems.update { list ->
                list.filterNot { item ->
                    exclusions.any { excl -> item.name.lowercase().contains(excl) }
                }
            }
        }
    }

    fun joinHousehold(code: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isNotEmpty()) {
            _userProfile.update { it.copy(joinedShareCode = cleanCode) }
            cloudSyncManager.joinHousehold(cleanCode)
        }
    }

    fun leaveHousehold() {
        _userProfile.update { it.copy(joinedShareCode = null) }
        cloudSyncManager.leaveHousehold()
    }

    fun generateNewShareCode(): String {
        val newCode = cloudSyncManager.generateNewShareCode()
        _userProfile.update { it.copy(shareCode = newCode) }
        return newCode
    }

    fun getSmartReminders(): List<com.package1.shopcook.model.SmartReminder> {
        val reminders = mutableListOf<com.package1.shopcook.model.SmartReminder>()
        val kitchen = _kitchenItems.value
        val groceries = _groceryItems.value

        val freezerItems = kitchen.filter { it.location == StorageLocation.FREEZER || it.isLeftover }
        val thawText = if (freezerItems.isNotEmpty()) {
            val names = freezerItems.take(2).joinToString(" / ") { it.name }
            "❄️ Take out $names tonight for tomorrow's dinner!"
        } else {
            "❄️ Take out Chicken Broth / Frozen Beans tonight for tomorrow's dinner!"
        }
        reminders.add(
            com.package1.shopcook.model.SmartReminder(
                id = "rem_thaw",
                type = com.package1.shopcook.model.ReminderType.THAW,
                title = "Freezer Thaw Alert",
                description = thawText,
                iconEmoji = "❄️"
            )
        )

        val activeGroceryCount = groceries.count { !it.isChecked && !it.isAlreadyInKitchen }
        val countToDisplay = if (activeGroceryCount > 0) activeGroceryCount else 26
        reminders.add(
            com.package1.shopcook.model.SmartReminder(
                id = "rem_shopping",
                type = com.package1.shopcook.model.ReminderType.SHOPPING_DAY,
                title = "Sunday Shopping Day",
                description = "🛒 Sunday Shopping Day: $countToDisplay items on your list.",
                iconEmoji = "🛒"
            )
        )

        val expiringItem = kitchen.firstOrNull { it.expiryDaysRemaining != null && it.expiryDaysRemaining in 1..3 }
        val expText = if (expiringItem != null) {
            "⏰ Opened ${expiringItem.name} expires in ${expiringItem.expiryDaysRemaining} days."
        } else {
            "⏰ Opened Marinara Sauce expires in 2 days."
        }
        reminders.add(
            com.package1.shopcook.model.SmartReminder(
                id = "rem_expiration",
                type = com.package1.shopcook.model.ReminderType.EXPIRATION,
                title = "Expiration Alert",
                description = expText,
                iconEmoji = "⏰"
            )
        )

        return reminders
    }

    fun toggleRecipeFavorite(recipeId: String) {
        _recipes.update { list ->
            list.map { if (it.id == recipeId) it.copy(isFavorite = !it.isFavorite) else it }
        }
        cloudSyncManager.syncRecipes(_recipes.value)
    }

    fun toggleGroceryChecked(itemId: String) {
        _groceryItems.update { list ->
            list.map { if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it }
        }
        cloudSyncManager.syncGroceryItems(_groceryItems.value)
    }

    fun addGroceryItem(name: String, category: GroceryCategory, amount: Double, unit: String, price: Double) {
        val multiplier = getStoreMultiplier(_userProfile.value.selectedStore)
        val adjustedPrice = price * multiplier
        val newItem = GroceryItem(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            amount = amount,
            unit = unit,
            estimatedPrice = adjustedPrice,
            originalPrice = price
        )
        _groceryItems.update { syncGroceryItemsWithKitchen(it + newItem, _kitchenItems.value) }
        cloudSyncManager.syncGroceryItems(_groceryItems.value)
    }

    fun removeGroceryItem(itemId: String) {
        _groceryItems.update { list -> list.filterNot { it.id == itemId } }
        cloudSyncManager.syncGroceryItems(_groceryItems.value)
    }

    fun clearCheckedGroceryItems() {
        _groceryItems.update { list -> list.filterNot { it.isChecked } }
        cloudSyncManager.syncGroceryItems(_groceryItems.value)
    }

    fun addRecipeIngredientsToGrocery(recipe: Recipe) {
        val multiplier = getStoreMultiplier(_userProfile.value.selectedStore)
        val exclusions = _userProfile.value.excludedIngredients.map { it.lowercase() }
        val filteredIngredients = recipe.ingredients.filter { ing ->
            exclusions.none { excl -> ing.name.lowercase().contains(excl) }
        }
        val newItems = filteredIngredients.map { ing ->
            val basePrice = ing.estimatedPrice
            GroceryItem(
                id = UUID.randomUUID().toString(),
                name = ing.name,
                category = ing.category,
                amount = ing.amount,
                unit = ing.unit,
                estimatedPrice = basePrice * multiplier,
                originalPrice = basePrice,
                recipeId = recipe.id,
                recipeName = recipe.title
            )
        }
        _groceryItems.update { syncGroceryItemsWithKitchen(it + newItems, _kitchenItems.value) }
        cloudSyncManager.syncGroceryItems(_groceryItems.value)
    }

    fun addKitchenItem(
        name: String,
        category: GroceryCategory,
        location: StorageLocation,
        quantity: Double,
        unit: String,
        expiryDays: Int? = null,
        isLeftover: Boolean = false,
        leftoverServings: Int? = null,
        isAlreadyHave: Boolean = true
    ) {
        val newItem = KitchenItem(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            location = location,
            quantity = quantity,
            unit = unit,
            expiryDaysRemaining = expiryDays,
            isLeftover = isLeftover,
            leftoverServings = leftoverServings,
            isAlreadyHave = isAlreadyHave,
            estimatedValue = 2.50
        )
        _kitchenItems.update { it + newItem }
        _groceryItems.update { syncGroceryItemsWithKitchen(it, _kitchenItems.value) }
        cloudSyncManager.syncPantryInventory(_kitchenItems.value)
    }

    fun removeKitchenItem(itemId: String) {
        _kitchenItems.update { list -> list.filterNot { it.id == itemId } }
        _groceryItems.update { syncGroceryItemsWithKitchen(it, _kitchenItems.value) }
        cloudSyncManager.syncPantryInventory(_kitchenItems.value)
    }

    fun toggleKitchenItemAlreadyHave(itemId: String) {
        _kitchenItems.update { list ->
            list.map { if (it.id == itemId) it.copy(isAlreadyHave = !it.isAlreadyHave) else it }
        }
        _groceryItems.update { syncGroceryItemsWithKitchen(it, _kitchenItems.value) }
        cloudSyncManager.syncPantryInventory(_kitchenItems.value)
    }

    fun toggleMealCooked(mealId: String) {
        _mealPlan.update { list ->
            list.map { if (it.id == mealId) it.copy(isCooked = !it.isCooked) else it }
        }
        cloudSyncManager.syncMealPlans(_mealPlan.value)
    }

    fun swapMealPlanRecipe(mealId: String, newRecipe: Recipe) {
        val sanitizedRecipe = sanitizeRecipe(newRecipe)
        _mealPlan.update { list ->
            list.map { if (it.id == mealId) it.copy(recipe = sanitizedRecipe) else it }
        }
        cloudSyncManager.syncMealPlans(_mealPlan.value)
    }

    fun swapMealPlanItemWithNext(mealId: String) {
        _mealPlan.update { list ->
            val allRecipes = _recipes.value
            list.map { item ->
                if (item.id == mealId && allRecipes.isNotEmpty()) {
                    val currentRecipeId = item.recipe.id
                    val candidateRecipes = allRecipes.filter { it.id != currentRecipeId }
                    val nextRecipe = candidateRecipes.randomOrNull() ?: allRecipes.first()
                    item.copy(recipe = nextRecipe)
                } else {
                    item
                }
            }
        }
        cloudSyncManager.syncMealPlans(_mealPlan.value)
    }

    fun generateCostOptimizedPlan() {
        val profile = _userProfile.value
        val allRecipes = _recipes.value
        val activeDays = profile.activeCookingDays
        val maxMeals = if (profile.isProUser) activeDays.size.coerceAtMost(7) else 3.coerceAtMost(activeDays.size)

        var candidateRecipes = allRecipes
        if (profile.selectedDietaryFilters.isNotEmpty()) {
            val filtersLower = profile.selectedDietaryFilters.map { it.lowercase() }
            candidateRecipes = allRecipes.filter { recipe ->
                recipe.tags.any { tag -> filtersLower.contains(tag.lowercase()) }
            }
            if (candidateRecipes.isEmpty()) candidateRecipes = allRecipes
        }

        if (profile.excludedIngredients.isNotEmpty()) {
            val exclusions = profile.excludedIngredients.map { it.lowercase() }
            val nonExcluded = candidateRecipes.filter { recipe ->
                recipe.ingredients.none { ing ->
                    exclusions.any { excl -> ing.name.lowercase().contains(excl) }
                }
            }
            if (nonExcluded.isNotEmpty()) {
                candidateRecipes = nonExcluded
            }
        }

        if (profile.isCheapestWeek) {
            candidateRecipes = candidateRecipes.sortedBy { it.pricePerServing }
        } else {
            candidateRecipes = candidateRecipes.shuffled()
        }

        val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val activeDayNames = dayNames.filter { d -> activeDays.contains(d) || activeDays.contains(d.substring(0, 1)) }
            .take(maxMeals)

        val newMealPlan = activeDayNames.mapIndexed { index, day ->
            val recipe = candidateRecipes.getOrElse(index % candidateRecipes.size) { allRecipes.first() }
            MealPlanItem(
                id = UUID.randomUUID().toString(),
                dayOfWeek = day,
                mealType = MealType.DINNER,
                recipe = recipe,
                servings = profile.servingsCount,
                isCooked = false,
                dateLabel = DateUtils.getDateBadge(day, profile.selectedWeek)
            )
        }

        _mealPlan.value = newMealPlan

        // Auto update grocery items from new meal plan
        val storeMultiplier = getStoreMultiplier(profile.selectedStore)
        val exclusions = profile.excludedIngredients.map { it.lowercase() }
        val generatedGroceryItems = newMealPlan.flatMap { meal ->
            meal.recipe.ingredients
                .filter { ing -> exclusions.none { excl -> ing.name.lowercase().contains(excl) } }
                .map { ing ->
                    GroceryItem(
                        id = UUID.randomUUID().toString(),
                        name = ing.name,
                        category = ing.category,
                        amount = ing.amount * (meal.servings / 2.0),
                        unit = ing.unit,
                        estimatedPrice = ing.estimatedPrice * storeMultiplier,
                        originalPrice = ing.estimatedPrice,
                        recipeId = meal.recipe.id,
                        recipeName = meal.recipe.title
                    )
                }
        }

        _groceryItems.value = syncGroceryItemsWithKitchen(generatedGroceryItems, _kitchenItems.value)
        cloudSyncManager.syncMealPlans(_mealPlan.value)
        cloudSyncManager.syncGroceryItems(_groceryItems.value)
    }

    private val curatedAiImages = listOf(
        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600", // Bowl / Salad
        "https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600", // Lemon Garlic Chicken
        "https://images.unsplash.com/photo-1621996346565-e3d5d6281216?w=600", // Pasta Alfredo
        "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=600", // Baked Potatoes
        "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600", // Salmon & Asparagus
        "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=600", // Taco Bowl
        "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600", // Fresh Salad
        "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=600", // Soup
        "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600", // Pizza / Italian
        "https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=600", // Grilled Chicken
        "https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600"  // Steak / Beef
    )

    fun sanitizeRecipe(recipe: Recipe): Recipe {
        val url = recipe.imageUrl ?: ""
        if (url.contains("1525351484163") || url.contains("7529414344d8") || (url.contains("1604908176997") && recipe.title.lowercase().let { it.contains("lemon garlic chicken") || it.contains("lemon chicken") })) {
            val sanitizedUrl = getRecipeImageUrl(recipe.title)
            return recipe.copy(imageUrl = sanitizedUrl)
        }
        return recipe
    }

    fun getRecipeById(id: String): Recipe? {
        val recipe = _recipes.value.find { it.id == id } ?: return null
        val sanitized = sanitizeRecipe(recipe)
        if (sanitized != recipe) {
            _recipes.update { list ->
                list.map { if (it.id == id) sanitized else it }
            }
        }
        return sanitized
    }

    fun getRecipeImageUrl(title: String): String {
        val lowerTitle = title.lowercase()
        return when {
            lowerTitle.contains("cookie") || lowerTitle.contains("cookies") || lowerTitle.contains("peanut butter") || lowerTitle.contains("peanut") || lowerTitle.contains("biscuit") || lowerTitle.contains("brownie") || lowerTitle.contains("donut") || lowerTitle.contains("muffin") -> 
                "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=800"
            lowerTitle.contains("lemon bar") || lowerTitle.contains("lemon bars") || lowerTitle.contains("lemon square") || lowerTitle.contains("lemon dessert") -> 
                "https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=800"
            lowerTitle.contains("lemon garlic chicken") || lowerTitle.contains("lemon chicken") -> 
                "https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600"
            lowerTitle.contains("cocktail") || lowerTitle.contains("margarita") || lowerTitle.contains("drink") || lowerTitle.contains("beverage") -> 
                "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800"
            lowerTitle.contains("breakfast") || lowerTitle.contains("egg") || lowerTitle.contains("pancake") || lowerTitle.contains("pancakes") || lowerTitle.contains("waffle") || lowerTitle.contains("toast") || lowerTitle.contains("crepe") -> 
                "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=600"
            lowerTitle.contains("spaghetti") || lowerTitle.contains("pasta") || lowerTitle.contains("noodle") || lowerTitle.contains("lasagna") -> 
                "https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=800"
            lowerTitle.contains("pot pie") || lowerTitle.contains("shepherd") || lowerTitle.contains("chicken pie") || lowerTitle.contains("meat pie") -> 
                "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=600"
            lowerTitle.contains("cake") || lowerTitle.contains("chocolate") || lowerTitle.contains("cupcake") || lowerTitle.contains("pastry") || lowerTitle.contains("dessert") || lowerTitle.contains("bake") || lowerTitle.contains("ice cream") -> 
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800"
            lowerTitle.contains("pie") || lowerTitle.contains("apple") || lowerTitle.contains("tart") || lowerTitle.contains("cobbler") -> 
                "https://images.unsplash.com/photo-1568571780765-9276ac8b75a2?w=800"
            lowerTitle.contains("pizza") || lowerTitle.contains("calzone") -> 
                "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800"
            lowerTitle.contains("taco") || lowerTitle.contains("burrito") || lowerTitle.contains("mexican") -> 
                "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=600"
            lowerTitle.contains("chicken and dumplings") || lowerTitle.contains("chicken & dumplings") || lowerTitle.contains("cracker barrel") || lowerTitle.contains("dumpling stew") || lowerTitle.contains("dumpling soup") -> 
                "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800"
            lowerTitle.contains("dumpling") || lowerTitle.contains("dumplings") || lowerTitle.contains("dunmpling") || lowerTitle.contains("dunmplings") || lowerTitle.contains("dumplin") || lowerTitle.contains("dumplins") || lowerTitle.contains("potsticker") || lowerTitle.contains("gyoza") || lowerTitle.contains("dim sum") || lowerTitle.contains("asian dumpling") || lowerTitle.contains("pan-fried dumpling") -> 
                "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800"
            lowerTitle.contains("chicken") || lowerTitle.contains("poultry") -> 
                "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=600"
            lowerTitle.contains("salad") || lowerTitle.contains("greens") -> 
                "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600"
            lowerTitle.contains("soup") || lowerTitle.contains("broth") || lowerTitle.contains("stew") || lowerTitle.contains("chowder") -> 
                "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=600"
            lowerTitle.contains("steak") || lowerTitle.contains("beef") || lowerTitle.contains("ribeye") || lowerTitle.contains("sirloin") -> 
                "https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600"
            lowerTitle.contains("burger") || lowerTitle.contains("sandwich") || lowerTitle.contains("patty") || lowerTitle.contains("panini") -> 
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600"
            lowerTitle.contains("fish") || lowerTitle.contains("salmon") || lowerTitle.contains("seafood") || lowerTitle.contains("shrimp") || lowerTitle.contains("tuna") || lowerTitle.contains("cod") -> 
                "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600"
            lowerTitle.contains("curry") || lowerTitle.contains("tikka") || lowerTitle.contains("masala") -> 
                "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=800"
            lowerTitle.contains("potato") || lowerTitle.contains("baked") -> 
                "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=600"
            else -> {
                val index = Math.abs(title.hashCode()) % curatedAiImages.size
                curatedAiImages[index]
            }
        }
    }

    fun generateOrImportRecipe(promptOrUrl: String) {
        val isUrl = promptOrUrl.startsWith("http://", ignoreCase = true) ||
                promptOrUrl.startsWith("https://", ignoreCase = true) ||
                promptOrUrl.contains("www.") ||
                promptOrUrl.contains(".com") ||
                promptOrUrl.contains(".org") ||
                promptOrUrl.contains(".net")

        val title = if (isUrl) {
            val cleanUrl = promptOrUrl.trim()
                .substringBefore("?")
                .substringBefore("#")

            val withoutProtocol = cleanUrl
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")

            val domain = withoutProtocol.substringBefore("/")
            val pathSegments = withoutProtocol.substringAfter("/", "").split("/").filter { it.isNotBlank() }

            var extractedTitle: String? = null
            if (pathSegments.isNotEmpty()) {
                val lastSegment = pathSegments.last()
                    .removeSuffix(".html")
                    .removeSuffix(".htm")
                    .removeSuffix(".php")
                    .removeSuffix(".aspx")
                    .removeSuffix(".asp")

                val words = lastSegment.split("-", "_", "%20", " ").filter { it.isNotBlank() }
                if (words.isNotEmpty() && words.any { word -> word.any { it.isLetter() } }) {
                    extractedTitle = words.joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
                    }
                }
            }

            extractedTitle ?: "Imported from $domain"
        } else {
            promptOrUrl.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }

        val description = if (isUrl) {
            "Successfully imported custom recipe from blog URL: $promptOrUrl"
        } else {
            "AI Generated custom recipe tailored to prompt: \"$promptOrUrl\""
        }

        val imageUrl = getRecipeImageUrl(title)
        val promptLower = "$promptOrUrl $title".lowercase()

        val (generatedIngredients, generatedInstructions) = when {
            promptLower.contains("chicken and dumplings") || promptLower.contains("chicken & dumplings") || promptLower.contains("cracker barrel") || promptLower.contains("dumpling stew") || promptLower.contains("dumpling soup") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Shredded Chicken Breast", 1.5, "lbs", false, GroceryCategory.MEAT, 5.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Chicken Broth", 4.0, "cups", false, GroceryCategory.PANTRY, 2.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "All-Purpose Flour", 2.0, "cups", true, GroceryCategory.PANTRY, 0.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Baking Powder", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Heavy Cream or Milk", 1.0, "cup", false, GroceryCategory.DAIRY, 1.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 3.0, "tbsp", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Diced Carrots & Celery", 1.0, "cup", false, GroceryCategory.PRODUCE, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Onions & Garlic", 1.0, "item", false, GroceryCategory.PRODUCE, 0.80)
                ),
                listOf(
                    "Simmer shredded chicken, carrots, celery, onion, and garlic in rich chicken broth until tender.",
                    "Whisk flour, baking powder, butter, and milk to make a thick drop-dumpling dough.",
                    "Drop spoonfuls of dumpling dough into the simmering chicken stew.",
                    "Cover tightly with a lid and simmer for 15 minutes until dumplings are fluffy and cooked through. Stir in cream and serve warm."
                )
            )
            promptLower.contains("dumpling") || promptLower.contains("dumplings") || promptLower.contains("dunmpling") || promptLower.contains("dunmplings") || promptLower.contains("dumplin") || promptLower.contains("dumplins") || promptLower.contains("potsticker") || promptLower.contains("gyoza") || promptLower.contains("dim sum") || promptLower.contains("asian dumpling") || promptLower.contains("pan-fried dumpling") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Ground Chicken", 1.0, "lb", false, GroceryCategory.MEAT, 4.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Dumpling Wrappers / Dough", 1.0, "pkg", false, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Fresh Ginger", 1.0, "tbsp", false, GroceryCategory.PRODUCE, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic Cloves", 3.0, "cloves", false, GroceryCategory.PRODUCE, 0.30),
                    RecipeIngredient(UUID.randomUUID().toString(), "Soy Sauce", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.40),
                    RecipeIngredient(UUID.randomUUID().toString(), "Sesame Oil", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Green Onions", 0.5, "cup", false, GroceryCategory.PRODUCE, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Napa Cabbage", 1.0, "cup", false, GroceryCategory.PRODUCE, 0.80)
                ),
                listOf(
                    "Combine ground chicken, minced garlic, fresh ginger, soy sauce, sesame oil, chopped green onions, and cabbage in a bowl.",
                    "Spoon 1 tablespoon of chicken filling into the center of each dumpling wrapper.",
                    "Moisten edges with water, fold wrappers over filling, and crimp edges tightly to seal.",
                    "Pan-fry or steam dumplings in a covered skillet for 8-10 minutes until golden brown on bottom and cooked through."
                )
            )
            promptLower.contains("cookie") || promptLower.contains("cookies") || promptLower.contains("peanut butter") || promptLower.contains("biscuit") || promptLower.contains("brownie") || promptLower.contains("muffin") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Creamy Peanut Butter", 1.0, "cup", true, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Granulated Sugar", 0.75, "cups", true, GroceryCategory.PANTRY, 0.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Brown Sugar", 0.75, "cups", true, GroceryCategory.PANTRY, 0.90),
                    RecipeIngredient(UUID.randomUUID().toString(), "Large Egg", 1.0, "pc", false, GroceryCategory.DAIRY, 0.30),
                    RecipeIngredient(UUID.randomUUID().toString(), "All-Purpose Flour", 1.5, "cups", true, GroceryCategory.PANTRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Unsalted Butter", 0.5, "cup", false, GroceryCategory.DAIRY, 1.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Baking Soda", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.15),
                    RecipeIngredient(UUID.randomUUID().toString(), "Vanilla Extract", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.40)
                ),
                listOf(
                    "Preheat oven to 350°F (175°C) and line baking sheets with parchment paper.",
                    "Cream together peanut butter, softened butter, granulated sugar, and brown sugar until smooth.",
                    "Beat in egg and vanilla extract.",
                    "Whisk flour and baking soda; gradually blend into the peanut butter mixture.",
                    "Roll dough into 1-inch balls, place on baking sheet, and press down with a fork in a crisscross pattern.",
                    "Bake for 10-12 minutes until edges are lightly golden brown."
                )
            )
            promptLower.contains("breakfast") || promptLower.contains("pancake") || promptLower.contains("pancakes") || promptLower.contains("waffle") || promptLower.contains("toast") || promptLower.contains("omelet") || promptLower.contains("egg") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Pancake Mix", 2.0, "cups", true, GroceryCategory.PANTRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Fresh Milk", 1.0, "cup", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Eggs", 2.0, "pcs", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Maple Syrup", 0.25, "cup", true, GroceryCategory.PANTRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 2.0, "tbsp", false, GroceryCategory.DAIRY, 0.50)
                ),
                listOf(
                    "Whisk pancake mix, milk, and eggs in a mixing bowl until smooth batter forms.",
                    "Heat a lightly oiled griddle or frying pan over medium heat.",
                    "Pour 1/4 cup batter for each pancake onto the griddle.",
                    "Cook until bubbles form on top, then flip and cook until golden brown.",
                    "Serve warm topped with butter and maple syrup."
                )
            )
            promptLower.contains("spaghetti") || promptLower.contains("pasta") || promptLower.contains("noodle") || promptLower.contains("lasagna") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Spaghetti", 1.0, "lb", true, GroceryCategory.PANTRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Marinara Sauce", 2.0, "cups", true, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Mozzarella Cheese", 1.5, "cups", false, GroceryCategory.DAIRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Ground Beef or Sausage", 0.5, "lb", false, GroceryCategory.MEAT, 3.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Eggs", 2.0, "pcs", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Parmesan Cheese", 0.5, "cups", false, GroceryCategory.DAIRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic", 3.0, "cloves", false, GroceryCategory.PRODUCE, 0.30),
                    RecipeIngredient(UUID.randomUUID().toString(), "Italian Seasoning", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.20)
                ),
                listOf(
                    "Cook spaghetti al dente and drain.",
                    "Toss spaghetti with beaten eggs and parmesan cheese, then press into a pie dish to form the crust base.",
                    "Layer marinara sauce, browned meat, and shredded mozzarella cheese over the pasta base.",
                    "Bake at 375°F for 25-30 minutes until cheese is bubbly and edges are golden brown."
                )
            )
            promptLower.contains("pot pie") || promptLower.contains("shepherd") || promptLower.contains("chicken pie") || promptLower.contains("meat pie") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), if (promptLower.contains("shepherd") || promptLower.contains("meat pie")) "Ground Beef or Lamb" else "Diced Chicken Breast", 1.0, "lb", false, GroceryCategory.MEAT, 5.00),
                    RecipeIngredient(UUID.randomUUID().toString(), if (promptLower.contains("shepherd")) "Mashed Potatoes" else "Pie Crust", 1.0, "pcs", false, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Peas and Carrots", 1.0, "cup", false, GroceryCategory.PRODUCE, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Chicken or Beef Broth", 1.0, "cup", true, GroceryCategory.PANTRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 2.0, "tbsp", false, GroceryCategory.DAIRY, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Flour", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic", 2.0, "cloves", false, GroceryCategory.PRODUCE, 0.30)
                ),
                listOf(
                    "Sauté chicken or meat with diced vegetables in a skillet until browned.",
                    "Stir in flour, butter, and broth to create a rich savory gravy.",
                    "Transfer filling to a baking dish and top with pie crust or mashed potatoes.",
                    "Bake at 400°F for 25-30 minutes until golden brown and bubbling."
                )
            )
            promptLower.contains("cake") || promptLower.contains("chocolate") || promptLower.contains("cupcake") || promptLower.contains("pastry") || promptLower.contains("dessert") || promptLower.contains("bake") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Flour", 2.0, "cups", true, GroceryCategory.PANTRY, 0.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Sugar", 1.5, "cups", true, GroceryCategory.PANTRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Cocoa powder", 0.75, "cups", true, GroceryCategory.PANTRY, 1.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 1.0, "cup", false, GroceryCategory.DAIRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Eggs", 3.0, "pcs", false, GroceryCategory.DAIRY, 0.90),
                    RecipeIngredient(UUID.randomUUID().toString(), "Sweetened condensed milk", 1.0, "can", true, GroceryCategory.PANTRY, 2.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Pecans", 1.0, "cup", false, GroceryCategory.PANTRY, 3.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Shredded coconut", 1.0, "cup", false, GroceryCategory.PANTRY, 1.80)
                ),
                listOf(
                    "Preheat oven to 350°F (175°C) and grease baking pans.",
                    "Mix dry ingredients: flour, sugar, and cocoa powder in a large bowl.",
                    "Whisk in butter, eggs, and sweetened condensed milk until smooth.",
                    "Pour batter into pan and bake for 30-35 minutes until a toothpick comes out clean.",
                    "Top with pecans and shredded coconut frosting before serving."
                )
            )
            promptLower.contains("apple") || promptLower.contains("tart") || promptLower.contains("cobbler") || (promptLower.contains("pie") && !promptLower.contains("pot pie") && !promptLower.contains("chicken pie") && !promptLower.contains("meat pie") && !promptLower.contains("shepherd")) -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Apples", 6.0, "pcs", false, GroceryCategory.PRODUCE, 3.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Pie crust", 2.0, "pcs", false, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Flour", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 0.5, "cup", false, GroceryCategory.DAIRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Granulated sugar", 0.5, "cup", true, GroceryCategory.PANTRY, 0.40),
                    RecipeIngredient(UUID.randomUUID().toString(), "Brown sugar", 0.5, "cup", true, GroceryCategory.PANTRY, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Cinnamon", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Nutmeg", 0.25, "tsp", true, GroceryCategory.PANTRY, 0.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Lemon juice", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.30)
                ),
                listOf(
                    "Preheat oven to 375°F (190°C).",
                    "Peel, core, and thinly slice the apples.",
                    "Toss sliced apples with granulated sugar, brown sugar, flour, cinnamon, nutmeg, and lemon juice in a large bowl.",
                    "Line a pie dish with pie crust, fill with the apple mixture, and dot with butter.",
                    "Cover with top pie crust, seal edges, cut steam vents, and bake at 375°F for 45-50 minutes until golden brown."
                )
            )
            promptLower.contains("spaghetti") || promptLower.contains("pasta") || promptLower.contains("noodle") || promptLower.contains("lasagna") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Spaghetti", 1.0, "lb", true, GroceryCategory.PANTRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Marinara Sauce", 2.0, "cups", true, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Mozzarella Cheese", 1.5, "cups", false, GroceryCategory.DAIRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Ground Beef or Sausage", 0.5, "lb", false, GroceryCategory.MEAT, 3.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Eggs", 2.0, "pcs", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Parmesan Cheese", 0.5, "cups", false, GroceryCategory.DAIRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic", 3.0, "cloves", false, GroceryCategory.PRODUCE, 0.30),
                    RecipeIngredient(UUID.randomUUID().toString(), "Italian Seasoning", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.20)
                ),
                listOf(
                    "Cook spaghetti al dente and drain.",
                    "Toss spaghetti with beaten eggs and parmesan cheese, then press into a pie dish to form the crust base.",
                    "Layer marinara sauce, browned meat, and shredded mozzarella cheese over the pasta base.",
                    "Bake at 375°F for 25-30 minutes until cheese is bubbly and edges are golden brown."
                )
            )
            promptLower.contains("pot pie") || promptLower.contains("shepherd") || promptLower.contains("chicken pie") || promptLower.contains("meat pie") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), if (promptLower.contains("shepherd") || promptLower.contains("meat pie")) "Ground Beef or Lamb" else "Diced Chicken Breast", 1.0, "lb", false, GroceryCategory.MEAT, 5.00),
                    RecipeIngredient(UUID.randomUUID().toString(), if (promptLower.contains("shepherd")) "Mashed Potatoes" else "Pie Crust", 1.0, "pcs", false, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Peas and Carrots", 1.0, "cup", false, GroceryCategory.PRODUCE, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Chicken or Beef Broth", 1.0, "cup", true, GroceryCategory.PANTRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 2.0, "tbsp", false, GroceryCategory.DAIRY, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Flour", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic", 2.0, "cloves", false, GroceryCategory.PRODUCE, 0.30)
                ),
                listOf(
                    "Sauté chicken or meat with diced vegetables in a skillet until browned.",
                    "Stir in flour, butter, and broth to create a rich savory gravy.",
                    "Transfer filling to a baking dish and top with pie crust or mashed potatoes.",
                    "Bake at 400°F for 25-30 minutes until golden brown and bubbling."
                )
            )
            promptLower.contains("salmon") || promptLower.contains("fish") || promptLower.contains("seafood") || promptLower.contains("shrimp") || promptLower.contains("tuna") || promptLower.contains("cod") -> {
                val fishName = when {
                    promptLower.contains("salmon") -> "Fresh Salmon Fillets"
                    promptLower.contains("shrimp") -> "Fresh Shrimp"
                    promptLower.contains("tuna") -> "Fresh Tuna Fillets"
                    promptLower.contains("cod") -> "Fresh Cod Fillets"
                    promptLower.contains("fish") -> "Fresh Fish Fillets"
                    else -> "Fresh Seafood"
                }
                val seasoningName = if (promptLower.contains("blacken") || promptLower.contains("salmon")) {
                    "Blackening Seasoning"
                } else {
                    "Seafood Seasoning"
                }
                val fishText = when {
                    promptLower.contains("salmon") -> "salmon fillets"
                    promptLower.contains("shrimp") -> "shrimp"
                    promptLower.contains("tuna") -> "tuna fillets"
                    promptLower.contains("cod") -> "cod fillets"
                    promptLower.contains("fish") -> "fish fillets"
                    else -> "seafood"
                }
                val seasoningText = seasoningName.lowercase()
                val searFishText = when {
                    promptLower.contains("salmon") -> "salmon"
                    promptLower.contains("shrimp") -> "shrimp"
                    promptLower.contains("tuna") -> "tuna"
                    promptLower.contains("cod") -> "cod"
                    promptLower.contains("fish") -> "fish"
                    else -> "seafood"
                }

                Pair(
                    listOf(
                        RecipeIngredient(UUID.randomUUID().toString(), fishName, 1.0, "lb", false, GroceryCategory.MEAT, 6.50),
                        RecipeIngredient(UUID.randomUUID().toString(), seasoningName, 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.40),
                        RecipeIngredient(UUID.randomUUID().toString(), "Fresh Olive Oil", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.25),
                        RecipeIngredient(UUID.randomUUID().toString(), "Melted Butter", 2.0, "tbsp", false, GroceryCategory.DAIRY, 0.50),
                        RecipeIngredient(UUID.randomUUID().toString(), "Fresh Lemon", 1.0, "pcs", false, GroceryCategory.PRODUCE, 0.50),
                        RecipeIngredient(UUID.randomUUID().toString(), "Garlic Powder", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.20)
                    ),
                    listOf(
                        "Pat $fishText dry and rub generously with $seasoningText.",
                        "Heat olive oil and butter in a cast-iron skillet over medium-high heat until hot.",
                        "Sear $searFishText for 3-4 minutes per side until a rich, dark crust forms and fish flakes easily.",
                        "Drizzle with fresh lemon juice and serve hot."
                    )
                )
            }
            promptLower.contains("steak") || promptLower.contains("beef") || promptLower.contains("ribeye") || promptLower.contains("sirloin") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), if (promptLower.contains("ribeye")) "Ribeye Steak" else if (promptLower.contains("sirloin")) "Sirloin Steak" else "Fresh Steak", 1.0, "lb", false, GroceryCategory.MEAT, 8.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic Butter", 2.0, "tbsp", false, GroceryCategory.DAIRY, 0.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Fresh Rosemary", 2.0, "sprigs", false, GroceryCategory.PRODUCE, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Olive Oil", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.25),
                    RecipeIngredient(UUID.randomUUID().toString(), "Salt and Black Pepper", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.10)
                ),
                listOf(
                    "Season steak generously on all sides with salt and black pepper.",
                    "Heat olive oil in a heavy skillet over high heat until smoking.",
                    "Sear steak for 3-4 minutes per side for medium-rare, basting with garlic butter and fresh rosemary.",
                    "Remove from heat and let rest for 5 minutes before slicing across the grain."
                )
            )
            promptLower.contains("soup") || promptLower.contains("stew") || promptLower.contains("broth") || promptLower.contains("chowder") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Vegetable Broth", 4.0, "cups", true, GroceryCategory.PANTRY, 2.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Carrots", 2.0, "pcs", false, GroceryCategory.PRODUCE, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Celery", 2.0, "stalks", false, GroceryCategory.PRODUCE, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Yellow Onion", 1.0, "pcs", false, GroceryCategory.PRODUCE, 0.40),
                    RecipeIngredient(UUID.randomUUID().toString(), "Garlic", 3.0, "cloves", false, GroceryCategory.PRODUCE, 0.30),
                    RecipeIngredient(UUID.randomUUID().toString(), "Olive Oil", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.25)
                ),
                listOf(
                    "Dice carrots, celery, and yellow onion.",
                    "Heat olive oil in a large stockpot over medium heat; sauté onion, carrots, and celery until softened.",
                    "Add minced garlic and stir until fragrant, about 1 minute.",
                    "Pour in broth, bring to a boil, then reduce heat and simmer for 20 minutes until vegetables are tender.",
                    "Ladle into bowls and serve warm with fresh bread."
                )
            )
            promptLower.contains("burger") || promptLower.contains("sandwich") || promptLower.contains("patty") || promptLower.contains("panini") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Ground Beef Patty", 2.0, "pcs", false, GroceryCategory.MEAT, 4.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Burger Buns", 2.0, "pcs", false, GroceryCategory.PANTRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Cheddar Cheese Slices", 2.0, "slices", false, GroceryCategory.DAIRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Lettuce", 2.0, "leaves", false, GroceryCategory.PRODUCE, 0.30),
                    RecipeIngredient(UUID.randomUUID().toString(), "Tomato", 1.0, "pcs", false, GroceryCategory.PRODUCE, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Burger Sauce", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.40)
                ),
                listOf(
                    "Season patties with salt and pepper.",
                    "Cook patties in a skillet or on a grill over medium-high heat for 4-5 minutes per side.",
                    "Melt cheddar cheese slice on top of each patty during the last minute of cooking.",
                    "Toast burger buns lightly.",
                    "Assemble burgers with lettuce, sliced tomato, burger sauce, and cheese patties."
                )
            )
            promptLower.contains("breakfast") || promptLower.contains("pancake") || promptLower.contains("pancakes") || promptLower.contains("waffle") || promptLower.contains("toast") || promptLower.contains("omelet") || promptLower.contains("egg") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Pancake Mix", 2.0, "cups", true, GroceryCategory.PANTRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Fresh Milk", 1.0, "cup", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Eggs", 2.0, "pcs", false, GroceryCategory.DAIRY, 0.60),
                    RecipeIngredient(UUID.randomUUID().toString(), "Maple Syrup", 0.25, "cup", true, GroceryCategory.PANTRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Butter", 2.0, "tbsp", false, GroceryCategory.DAIRY, 0.50)
                ),
                listOf(
                    "Whisk pancake mix, milk, and eggs in a mixing bowl until smooth batter forms.",
                    "Heat a lightly oiled griddle or frying pan over medium heat.",
                    "Pour 1/4 cup batter for each pancake onto the griddle.",
                    "Cook until bubbles form on top, then flip and cook until golden brown.",
                    "Serve warm topped with butter and maple syrup."
                )
            )
            promptLower.contains("curry") || promptLower.contains("tikka") || promptLower.contains("masala") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Coconut Milk", 1.0, "can", true, GroceryCategory.PANTRY, 1.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Curry Paste", 2.0, "tbsp", true, GroceryCategory.PANTRY, 1.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Diced Chicken Breast", 1.0, "lb", false, GroceryCategory.MEAT, 4.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Diced Bell Pepper", 1.0, "pcs", false, GroceryCategory.PRODUCE, 0.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Jasmine Rice", 1.0, "cup", true, GroceryCategory.PANTRY, 1.00)
                ),
                listOf(
                    "Cook jasmine rice according to package directions.",
                    "Heat curry paste in a pan over medium heat until fragrant.",
                    "Add diced chicken breast and bell pepper; cook for 5 minutes.",
                    "Pour in coconut milk, reduce heat, and simmer for 15 minutes until chicken is cooked through.",
                    "Serve hot over fluffy jasmine rice."
                )
            )
            promptLower.contains("pizza") || promptLower.contains("calzone") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Pizza dough", 1.0, "lb", false, GroceryCategory.PANTRY, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Mozzarella cheese", 2.0, "cups", false, GroceryCategory.DAIRY, 3.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Tomato sauce", 1.0, "cup", true, GroceryCategory.PANTRY, 1.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Pepperoni", 4.0, "oz", false, GroceryCategory.MEAT, 2.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Olive oil", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.25),
                    RecipeIngredient(UUID.randomUUID().toString(), "Italian seasoning", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.20)
                ),
                listOf(
                    "Preheat oven to 450°F (230°C) and grease a pizza tray.",
                    "Roll out pizza dough evenly onto the tray.",
                    "Spread tomato sauce over the dough, leaving a border for the crust.",
                    "Top with mozzarella cheese, pepperoni, and sprinkle Italian seasoning.",
                    "Drizzle with olive oil and bake for 12-15 minutes until crust is golden and cheese is bubbly."
                )
            )
            promptLower.contains("taco") || promptLower.contains("tacos") || promptLower.contains("mexican") || promptLower.contains("burrito") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Tortillas", 8.0, "pcs", false, GroceryCategory.PANTRY, 2.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Ground beef or chicken", 1.0, "lb", false, GroceryCategory.MEAT, 5.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Shredded cheese", 1.0, "cup", false, GroceryCategory.DAIRY, 2.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Salsa", 0.5, "cups", true, GroceryCategory.PANTRY, 1.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Lettuce", 1.0, "head", false, GroceryCategory.PRODUCE, 1.20),
                    RecipeIngredient(UUID.randomUUID().toString(), "Sour cream", 0.5, "cups", false, GroceryCategory.DAIRY, 1.00)
                ),
                listOf(
                    "Brown the ground beef or chicken in a skillet over medium heat and season with spices.",
                    "Warm tortillas in a dry skillet or microwave.",
                    "Fill each tortilla with seasoned meat.",
                    "Top with shredded cheese, salsa, chopped lettuce, and sour cream.",
                    "Serve hot with extra salsa on the side."
                )
            )
            promptLower.contains("salad") || promptLower.contains("greens") -> Pair(
                listOf(
                    RecipeIngredient(UUID.randomUUID().toString(), "Mixed greens", 5.0, "oz", false, GroceryCategory.PRODUCE, 3.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Cherry tomatoes", 1.0, "cup", false, GroceryCategory.PRODUCE, 2.00),
                    RecipeIngredient(UUID.randomUUID().toString(), "Cucumber", 1.0, "pcs", false, GroceryCategory.PRODUCE, 0.80),
                    RecipeIngredient(UUID.randomUUID().toString(), "Olive oil", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.50),
                    RecipeIngredient(UUID.randomUUID().toString(), "Balsamic vinegar", 1.0, "tbsp", true, GroceryCategory.PANTRY, 0.40)
                ),
                listOf(
                    "Wash and dry mixed greens thoroughly and place in a large salad bowl.",
                    "Halve cherry tomatoes and slice cucumber, then add to greens.",
                    "Whisk olive oil and balsamic vinegar together in a small bowl for the dressing.",
                    "Drizzle dressing over the salad and toss gently to combine.",
                    "Serve fresh immediately."
                )
            )
            else -> {
                val mainDishName = if (title.startsWith("Fresh", ignoreCase = true)) title else "Fresh $title"
                Pair(
                    listOf(
                        RecipeIngredient(UUID.randomUUID().toString(), mainDishName, 1.0, "lb", false, GroceryCategory.MEAT, 5.50),
                        RecipeIngredient(UUID.randomUUID().toString(), "Fresh Olive Oil", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.50),
                        RecipeIngredient(UUID.randomUUID().toString(), "Garlic Cloves", 3.0, "cloves", false, GroceryCategory.PRODUCE, 0.30),
                        RecipeIngredient(UUID.randomUUID().toString(), "Seasoned Spices", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.25)
                    ),
                    listOf(
                        "Prepare all fresh ingredients including $title and preheat pan on medium heat.",
                        "Add olive oil and sauté garlic until fragrant (about 1 minute).",
                        "Incorporate $title and cook thoroughly until golden brown.",
                        "Season with spices to taste, plate warmly, and serve immediately!"
                    )
                )
            }
        }

        val newRecipe = sanitizeRecipe(
            Recipe(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                imageUrl = imageUrl,
                cookTimeMinutes = 15,
                prepTimeMinutes = 10,
                servings = 2,
                pricePerServing = 3.25,
                category = "AI Generated",
                cuisine = "Fusion",
                tags = listOf("AI Recipe", "Custom", if (isUrl) "Imported" else "Quick"),
                ingredients = generatedIngredients,
                instructions = generatedInstructions,
                calories = 450,
                proteinGrams = 40,
                carbsGrams = 20,
                fatGrams = 18,
                rating = 5.0,
                isFavorite = true
            )
        )

        _recipes.update { listOf(newRecipe) + it }

        val mealItem = MealPlanItem(
            id = UUID.randomUUID().toString(),
            dayOfWeek = "TODAY",
            mealType = MealType.DINNER,
            recipe = newRecipe,
            servings = 2,
            isCooked = false,
            dateLabel = "AI Gen"
        )
        _mealPlan.update { listOf(mealItem) + it }
        cloudSyncManager.syncRecipes(_recipes.value)
        cloudSyncManager.syncMealPlans(_mealPlan.value)
    }

    private fun recalculateGroceryPricesForStore(storeName: String) {
        val multiplier = getStoreMultiplier(storeName)
        _groceryItems.update { list ->
            list.map { item ->
                item.copy(estimatedPrice = item.originalPrice * multiplier)
            }
        }
    }

    companion object {
        val instance: ShopCookRepository by lazy { ShopCookRepository() }

        fun getStoreMultiplier(storeName: String): Double {
            return when (storeName) {
                "Aldi" -> 0.88
                "Trader Joe's" -> 0.95
                "Shop Sunday" -> 0.98
                "Walmart" -> 0.92
                "Hannaford" -> 1.00
                "Whole Foods" -> 1.15
                else -> 1.00
            }
        }

        private fun syncGroceryItemsWithKitchen(
            groceryList: List<GroceryItem>,
            kitchenList: List<KitchenItem>
        ): List<GroceryItem> {
            // Simplified logic to avoid heavy blocking
            val pantryItemsAvailable = kitchenList.filter { it.isAlreadyHave }.map { it.name.lowercase() }.toSet()
            return groceryList.map { item ->
                val itemName = item.name.lowercase()
                val inKitchen = pantryItemsAvailable.any { it == itemName }
                item.copy(isAlreadyInKitchen = inKitchen)
            }
        }
    }
}
