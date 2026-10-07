package com.package1.shopcook.data

import com.package1.shopcook.model.CleanupLevel
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.GroceryItem
import com.package1.shopcook.model.KitchenItem
import com.package1.shopcook.model.MealPlanItem
import com.package1.shopcook.model.MealType
import com.package1.shopcook.model.Recipe
import com.package1.shopcook.model.RecipeDifficulty
import com.package1.shopcook.model.RecipeIngredient
import com.package1.shopcook.model.StorageLocation
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.util.DateUtils

object MockData {

    val sampleRecipes: List<Recipe> = listOf(
        Recipe(
            id = "rec_1",
            title = "Lemon Garlic Chicken with Rice",
            description = "Tender chicken thighs seared in garlic butter and finished with fresh lemon juice served over fluffy jasmine rice.",
            imageUrl = "https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600",
            cookTimeMinutes = 25,
            prepTimeMinutes = 10,
            servings = 2,
            pricePerServing = 2.69,
            difficulty = RecipeDifficulty.EASY,
            cleanupLevel = CleanupLevel.LOW,
            category = "Quick & Easy",
            cuisine = "Mediterranean",
            tags = listOf("High Protein", "Under 30 Mins", "Gluten-Free", "Budget Friendly"),
            ingredients = listOf(
                RecipeIngredient("ing_101", "Chicken Thighs", 1.5, "lbs", false, GroceryCategory.MEAT, 4.50),
                RecipeIngredient("ing_102", "Lemons", 2.0, "pcs", false, GroceryCategory.PRODUCE, 0.80),
                RecipeIngredient("ing_103", "Garlic", 4.0, "cloves", true, GroceryCategory.PRODUCE, 0.20),
                RecipeIngredient("ing_104", "Jasmine Rice", 1.5, "cups", true, GroceryCategory.PANTRY, 0.60),
                RecipeIngredient("ing_105", "Chicken Broth", 2.0, "cups", false, GroceryCategory.PANTRY, 1.20),
                RecipeIngredient("ing_106", "Olive Oil", 2.0, "tbsp", true, GroceryCategory.PANTRY, 0.30),
                RecipeIngredient("ing_107", "Butter", 1.0, "tbsp", true, GroceryCategory.DAIRY, 0.25)
            ),
            instructions = listOf(
                "Season chicken thighs generously with salt, pepper, and garlic powder.",
                "Heat olive oil in a skillet over medium-high heat. Sear chicken for 6-7 mins per side until golden brown.",
                "Remove chicken and melt butter in the same skillet. Add minced garlic and sauté for 1 minute.",
                "Pour in chicken broth and lemon juice, scaping up golden browned bits.",
                "Return chicken to pan, simmer for 5 mins, and serve hot over cooked jasmine rice."
            ),
            calories = 520,
            proteinGrams = 42,
            carbsGrams = 45,
            fatGrams = 18,
            rating = 4.9,
            isFavorite = true
        ),
        Recipe(
            id = "rec_2",
            title = "Crock Pot Chicken Alfredo",
            description = "Rich and creamy slow-cooked Alfredo sauce with tender shredded chicken breast and penne pasta. Effortless dinner!",
            imageUrl = "https://images.unsplash.com/photo-1621996346565-e3d5d6281216",
            cookTimeMinutes = 35,
            prepTimeMinutes = 15,
            servings = 2,
            pricePerServing = 3.20,
            difficulty = RecipeDifficulty.EASY,
            cleanupLevel = CleanupLevel.LOW,
            category = "Comfort Food",
            cuisine = "Italian",
            tags = listOf("Slow Cooker", "Kid Friendly", "Meal Prep", "Creamy"),
            ingredients = listOf(
                RecipeIngredient("ing_201", "Chicken Breast", 2.0, "lbs", false, GroceryCategory.MEAT, 7.98),
                RecipeIngredient("ing_202", "Heavy Cream", 1.5, "cups", false, GroceryCategory.DAIRY, 2.79),
                RecipeIngredient("ing_203", "Parmesan Cheese", 1.0, "cup", false, GroceryCategory.DAIRY, 2.50),
                RecipeIngredient("ing_204", "Penne Pasta", 1.0, "lb", false, GroceryCategory.PANTRY, 1.29),
                RecipeIngredient("ing_205", "Garlic Powder", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.10),
                RecipeIngredient("ing_206", "Cream Cheese", 4.0, "oz", false, GroceryCategory.DAIRY, 1.20)
            ),
            instructions = listOf(
                "Place chicken breasts in slow cooker with heavy cream, cream cheese, butter, and seasonings.",
                "Cover and cook on LOW for 4 hours (or HIGH for 2.5 hours).",
                "Shred chicken directly in slow cooker using two forks.",
                "Stir in freshly grated parmesan cheese until silky smooth.",
                "Toss in freshly cooked penne pasta and serve warm."
            ),
            calories = 680,
            proteinGrams = 48,
            carbsGrams = 52,
            fatGrams = 32,
            rating = 4.8,
            isFavorite = false
        ),
        Recipe(
            id = "rec_3",
            title = "Loaded Baked Potatoes with Broccoli & Cheddar",
            description = "Crispy skin Russet potatoes stuffed with steamed fresh broccoli florets, sharp cheddar cheese, and Greek yogurt.",
            imageUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47",
            cookTimeMinutes = 45,
            prepTimeMinutes = 10,
            servings = 2,
            pricePerServing = 1.95,
            difficulty = RecipeDifficulty.EASY,
            cleanupLevel = CleanupLevel.LOW,
            category = "Budget Friendly",
            cuisine = "American",
            tags = listOf("Vegetarian", "Budget Friendly", "High Fiber", "Low Cost"),
            ingredients = listOf(
                RecipeIngredient("ing_301", "Russet Potatoes", 4.0, "large", false, GroceryCategory.PRODUCE, 3.99),
                RecipeIngredient("ing_302", "Fresh Broccoli", 2.0, "cups", false, GroceryCategory.PRODUCE, 2.49),
                RecipeIngredient("ing_303", "Sharp Cheddar", 1.5, "cups", false, GroceryCategory.DAIRY, 3.29),
                RecipeIngredient("ing_304", "Plain Greek Yogurt", 0.5, "cup", false, GroceryCategory.DAIRY, 0.80),
                RecipeIngredient("ing_305", "Green Onions", 3.0, "stalks", false, GroceryCategory.PRODUCE, 0.50)
            ),
            instructions = listOf(
                "Scrub potatoes, prick with fork, rub with olive oil and salt. Bake at 400°F (200°C) for 45 mins until tender.",
                "Steam broccoli florets until bright green and tender-crisp (3 mins).",
                "Slice potatoes open, fluff interior with fork, and mix in Greek yogurt.",
                "Mound with steamed broccoli and cover generously with sharp cheddar.",
                "Broil for 2 mins until melted and bubbly. Top with chopped green onions."
            ),
            calories = 410,
            proteinGrams = 16,
            carbsGrams = 58,
            fatGrams = 14,
            rating = 4.7,
            isFavorite = true
        ),
        Recipe(
            id = "rec_4",
            title = "Mujadara",
            description = "Classic Middle Eastern comfort dish with spiced brown lentils, rice, and sweet deeply caramelized onions.",
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
            cookTimeMinutes = 40,
            prepTimeMinutes = 15,
            servings = 2,
            pricePerServing = 1.25,
            difficulty = RecipeDifficulty.MEDIUM,
            cleanupLevel = CleanupLevel.MEDIUM,
            category = "Budget Friendly",
            cuisine = "Middle Eastern",
            tags = listOf("Vegan", "Budget Champion", "High Fiber", "Pantry Meal"),
            ingredients = listOf(
                RecipeIngredient("ing_401", "Brown Lentils", 1.0, "cup", true, GroceryCategory.PANTRY, 0.90),
                RecipeIngredient("ing_402", "Basmati Rice", 1.0, "cup", true, GroceryCategory.PANTRY, 0.60),
                RecipeIngredient("ing_403", "Yellow Onions", 3.0, "large", false, GroceryCategory.PRODUCE, 1.50),
                RecipeIngredient("ing_404", "Olive Oil", 0.25, "cup", true, GroceryCategory.PANTRY, 0.50),
                RecipeIngredient("ing_405", "Cumin", 1.5, "tsp", true, GroceryCategory.PANTRY, 0.15),
                RecipeIngredient("ing_406", "Allspice", 0.5, "tsp", true, GroceryCategory.PANTRY, 0.10)
            ),
            instructions = listOf(
                "Thinly slice yellow onions. Heat olive oil in skillet and cook onions low & slow for 25 mins until dark golden brown.",
                "Simmer lentils in 3 cups salted water for 15 mins until partially cooked.",
                "Add rice, cumin, allspice, salt, and remaining water to lentils. Cover and simmer 20 mins.",
                "Fluff rice and lentils with fork, top with crispy caramelized onions, and serve with cucumber yogurt salad."
            ),
            calories = 380,
            proteinGrams = 15,
            carbsGrams = 64,
            fatGrams = 8,
            rating = 4.8,
            isFavorite = false
        ),
        Recipe(
            id = "rec_5",
            title = "One-Pan Salmon with Asparagus & Lemon",
            description = "Flaky pan-roasted salmon fillets cooked alongside crisp asparagus in a single skillet.",
            imageUrl = "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2",
            cookTimeMinutes = 15,
            prepTimeMinutes = 10,
            servings = 2,
            pricePerServing = 5.40,
            difficulty = RecipeDifficulty.EASY,
            cleanupLevel = CleanupLevel.LOW,
            category = "Healthy & Quick",
            cuisine = "American",
            tags = listOf("Keto", "High Omega-3", "15-Min Dinner", "Low Carb"),
            ingredients = listOf(
                RecipeIngredient("ing_501", "Salmon Fillets", 2.0, "pcs", false, GroceryCategory.MEAT, 8.50),
                RecipeIngredient("ing_502", "Asparagus", 1.0, "bundle", false, GroceryCategory.PRODUCE, 2.99),
                RecipeIngredient("ing_503", "Lemon", 1.0, "pc", false, GroceryCategory.PRODUCE, 0.40),
                RecipeIngredient("ing_504", "Garlic Powder", 1.0, "tsp", true, GroceryCategory.PANTRY, 0.10)
            ),
            instructions = listOf(
                "Trim asparagus ends. Season salmon and asparagus with olive oil, salt, pepper, and garlic powder.",
                "Heat skillet over medium-high heat. Sear salmon skin-side down for 4 mins.",
                "Flip salmon, add asparagus around fillets, and squeeze fresh lemon juice over top.",
                "Cook for additional 4 mins until salmon flakes easily and asparagus is tender-crisp."
            ),
            calories = 490,
            proteinGrams = 40,
            carbsGrams = 12,
            fatGrams = 22,
            rating = 4.9,
            isFavorite = true
        ),
        Recipe(
            id = "rec_6",
            title = "Taco Night Beef Bowls",
            description = "Seasoned lean ground beef with black beans, sweet corn, salsa, and avocado over cilantro rice.",
            imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b",
            cookTimeMinutes = 20,
            prepTimeMinutes = 10,
            servings = 2,
            pricePerServing = 3.10,
            difficulty = RecipeDifficulty.EASY,
            cleanupLevel = CleanupLevel.LOW,
            category = "Meal Prep",
            cuisine = "Mexican",
            tags = listOf("High Protein", "Family Favorite", "Customizable"),
            ingredients = listOf(
                RecipeIngredient("ing_601", "Lean Ground Beef", 1.0, "lb", false, GroceryCategory.MEAT, 5.49),
                RecipeIngredient("ing_602", "Black Beans", 1.0, "can", true, GroceryCategory.PANTRY, 0.99),
                RecipeIngredient("ing_603", "Sweet Corn", 1.0, "cup", true, GroceryCategory.FROZEN, 0.80),
                RecipeIngredient("ing_604", "Taco Seasoning", 1.0, "packet", true, GroceryCategory.PANTRY, 0.79),
                RecipeIngredient("ing_605", "Avocado", 1.0, "pc", false, GroceryCategory.PRODUCE, 1.25)
            ),
            instructions = listOf(
                "Brown ground beef in skillet, drain excess fat, stir in taco seasoning and 1/3 cup water.",
                "Simmer for 5 mins until sauce thickens.",
                "Warm black beans and sweet corn.",
                "Assemble bowls: layer cilantro rice, taco beef, corn, black beans, salsa, and avocado slices."
            ),
            calories = 580,
            proteinGrams = 38,
            carbsGrams = 56,
            fatGrams = 20,
            rating = 4.8,
            isFavorite = false
        )
    )

    val sampleGroceryItems: List<GroceryItem> = listOf(
        GroceryItem("g_1", "Fresh Broccoli", GroceryCategory.PRODUCE, 1.0, "head", 2.49, false, "rec_3", "Loaded Baked Potatoes"),
        GroceryItem("g_2", "Chicken Breast", GroceryCategory.MEAT, 2.0, "lbs", 7.98, false, "rec_2", "Crock Pot Chicken Alfredo"),
        GroceryItem("g_3", "Sharp Cheddar Cheese", GroceryCategory.DAIRY, 8.0, "oz", 3.29, false, "rec_3", "Loaded Baked Potatoes"),
        GroceryItem("g_4", "Russet Potatoes", GroceryCategory.PRODUCE, 5.0, "lbs", 3.99, true, "rec_3", "Loaded Baked Potatoes"),
        GroceryItem("g_5", "Heavy Cream", GroceryCategory.DAIRY, 1.0, "pint", 2.79, false, "rec_2", "Crock Pot Chicken Alfredo"),
        GroceryItem("g_6", "Penne Pasta", GroceryCategory.PANTRY, 1.0, "box", 1.29, true, "rec_2", "Crock Pot Chicken Alfredo"),
        GroceryItem("g_7", "Lemons", GroceryCategory.PRODUCE, 4.0, "pcs", 2.00, false, "rec_1", "Lemon Garlic Chicken"),
        GroceryItem("g_8", "Ground Beef", GroceryCategory.MEAT, 1.0, "lb", 5.49, false, "rec_6", "Taco Night Beef Bowls"),
        GroceryItem("g_9", "Canned Black Beans", GroceryCategory.PANTRY, 2.0, "cans", 1.98, true, "rec_6", "Taco Night Beef Bowls"),
        GroceryItem("g_10", "Avocado", GroceryCategory.PRODUCE, 2.0, "pcs", 2.50, false, "rec_6", "Taco Night Beef Bowls")
    )

    val sampleKitchenItems: List<KitchenItem> = listOf(
        // Pantry & Staples Checklist items
        KitchenItem("k_s1", "Apple Cider Vinegar", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "bottle", isAlreadyHave = true, estimatedValue = 3.20),
        KitchenItem("k_s2", "BBQ Sauce", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "bottle", isAlreadyHave = true, estimatedValue = 2.80),
        KitchenItem("k_s3", "Baking Powder", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "can", isAlreadyHave = true, estimatedValue = 1.90),
        KitchenItem("k_s4", "Baking Soda", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "box", isAlreadyHave = true, estimatedValue = 1.10),
        KitchenItem("k_s5", "Balsamic Vinegar", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "bottle", isAlreadyHave = true, estimatedValue = 4.50),
        KitchenItem("k_s6", "Brown Sugar", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "bag", isAlreadyHave = true, estimatedValue = 2.40),
        KitchenItem("k_s7", "Extra Virgin Olive Oil", GroceryCategory.PANTRY, StorageLocation.PANTRY, 0.5, "bottle", isLow = true, isAlreadyHave = true, estimatedValue = 8.50),
        KitchenItem("k_s8", "Garlic Powder", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "jar", isAlreadyHave = true, estimatedValue = 2.20),
        KitchenItem("k_s9", "Ground Cumin", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "jar", isAlreadyHave = true, estimatedValue = 2.10),
        KitchenItem("k_s10", "Soy Sauce", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "bottle", isAlreadyHave = true, estimatedValue = 2.90),
        KitchenItem("k_s11", "Ketchup", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "bottle", isAlreadyHave = true, estimatedValue = 2.50),
        KitchenItem("k_s12", "Black Pepper", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "grinder", isAlreadyHave = true, estimatedValue = 3.00),
        KitchenItem("k_s13", "Kosher Salt", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "box", isAlreadyHave = true, estimatedValue = 2.00),
        KitchenItem("k_s14", "Honey", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "jar", isAlreadyHave = true, estimatedValue = 4.80),

        // Kitchen Pantry items
        KitchenItem("k_1", "Jasmine Rice", GroceryCategory.PANTRY, StorageLocation.PANTRY, 2.5, "lbs", null, isLow = false, isAlreadyHave = true, estimatedValue = 3.50),
        KitchenItem("k_2", "Brown Lentils", GroceryCategory.PANTRY, StorageLocation.PANTRY, 1.0, "lb", null, isLow = false, isAlreadyHave = true, estimatedValue = 1.80),
        KitchenItem("k_5", "Taco Seasoning", GroceryCategory.PANTRY, StorageLocation.PANTRY, 2.0, "packets", null, isLow = false, isAlreadyHave = true, estimatedValue = 1.50),

        // Fridge items
        KitchenItem("k_7", "Whole Milk", GroceryCategory.DAIRY, StorageLocation.FRIDGE, 0.5, "gal", 4, isLow = true, isAlreadyHave = true, estimatedValue = 2.49),
        KitchenItem("k_8", "Unsalted Butter", GroceryCategory.DAIRY, StorageLocation.FRIDGE, 2.0, "sticks", 12, isLow = false, isAlreadyHave = true, estimatedValue = 2.20),
        KitchenItem("k_9", "Plain Greek Yogurt", GroceryCategory.DAIRY, StorageLocation.FRIDGE, 16.0, "oz", 6, isLow = false, isAlreadyHave = true, estimatedValue = 2.80),
        KitchenItem("k_10", "Large Eggs", GroceryCategory.DAIRY, StorageLocation.FRIDGE, 12.0, "pcs", 10, isLow = false, isAlreadyHave = true, estimatedValue = 3.20),
        KitchenItem("k_11", "Fresh Garlic", GroceryCategory.PRODUCE, StorageLocation.FRIDGE, 1.0, "head", 14, isLow = false, isAlreadyHave = true, estimatedValue = 0.80),

        // Freezer & Leftovers
        KitchenItem(
            id = "k_12",
            name = "Crock Pot Chicken Alfredo (Leftover)",
            category = GroceryCategory.MEAT,
            location = StorageLocation.FREEZER,
            quantity = 2.0,
            unit = "servings",
            expiryDaysRemaining = 30,
            isLow = false,
            isLeftover = true,
            leftoverServings = 2,
            addedDate = DateUtils.getUpcomingWeekRanges().first(),
            isAlreadyHave = true,
            estimatedValue = 6.40
        ),
        KitchenItem(
            id = "k_15",
            name = "Black beans, canned",
            category = GroceryCategory.PANTRY,
            location = StorageLocation.FREEZER,
            quantity = 2.0,
            unit = "cans",
            expiryDaysRemaining = 60,
            isLow = false,
            isLeftover = true,
            addedDate = DateUtils.getUpcomingWeekRanges().first(),
            isAlreadyHave = true,
            estimatedValue = 1.98
        ),
        KitchenItem(
            id = "k_16",
            name = "Chicken broth",
            category = GroceryCategory.PANTRY,
            location = StorageLocation.FREEZER,
            quantity = 1.0,
            unit = "container",
            expiryDaysRemaining = 45,
            isLow = false,
            isLeftover = true,
            addedDate = DateUtils.getUpcomingWeekRanges().first(),
            isAlreadyHave = true,
            estimatedValue = 2.50
        ),
        KitchenItem("k_13", "Frozen Organic Berries", GroceryCategory.FROZEN, StorageLocation.FREEZER, 1.0, "bag", 60, isLow = false, isAlreadyHave = true, estimatedValue = 4.20),
        KitchenItem("k_14", "Lean Ground Beef (Frozen)", GroceryCategory.MEAT, StorageLocation.FREEZER, 1.0, "lb", 45, isLow = false, isAlreadyHave = true, estimatedValue = 5.49)
    )

    val sampleMealPlan: List<MealPlanItem> = listOf(
        MealPlanItem("m_1", "MON", MealType.DINNER, sampleRecipes[0], servings = 2, isCooked = false, dateLabel = DateUtils.getDateBadge("MON", "This week")),
        MealPlanItem("m_2", "TUE", MealType.DINNER, sampleRecipes[1], servings = 2, isCooked = false, dateLabel = DateUtils.getDateBadge("TUE", "This week")),
        MealPlanItem("m_3", "WED", MealType.DINNER, sampleRecipes[2], servings = 2, isCooked = false, dateLabel = DateUtils.getDateBadge("WED", "This week")),
        MealPlanItem("m_4", "THU", MealType.DINNER, sampleRecipes[3], servings = 2, isCooked = false, dateLabel = DateUtils.getDateBadge("THU", "This week")),
        MealPlanItem("m_5", "FRI", MealType.DINNER, sampleRecipes[4], servings = 2, isCooked = false, dateLabel = DateUtils.getDateBadge("FRI", "This week")),
        MealPlanItem("m_6", "SAT", MealType.DINNER, sampleRecipes[5], servings = 2, isCooked = false, dateLabel = DateUtils.getDateBadge("SAT", "This week"))
    )

    val sampleUserProfile = UserProfile()
}
