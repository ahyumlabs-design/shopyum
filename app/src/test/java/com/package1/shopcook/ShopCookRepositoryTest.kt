package com.package1.shopcook

import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.Recipe
import com.package1.shopcook.model.StorageLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShopCookRepositoryTest {

    private lateinit var repository: ShopCookRepository

    @Before
    fun setUp() {
        repository = ShopCookRepository.instance
    }

    @Test
    fun testInitialMockDataLoaded() {
        val recipes = repository.recipes.value
        val groceryItems = repository.groceryItems.value
        val kitchenItems = repository.kitchenItems.value
        val mealPlan = repository.mealPlan.value

        assertTrue("Recipes list should not be empty", recipes.isNotEmpty())
        assertTrue("Grocery items should not be empty", groceryItems.isNotEmpty())
        assertTrue("Kitchen items should not be empty", kitchenItems.isNotEmpty())
        assertTrue("Meal plan should not be empty", mealPlan.isNotEmpty())

        // Check required mock recipes
        val titles = recipes.map { it.title }
        assertTrue("Should contain Lemon Garlic Chicken", titles.any { it.contains("Lemon Garlic Chicken") })
        assertTrue("Should contain Crock Pot Chicken Alfredo", titles.any { it.contains("Crock Pot Chicken Alfredo") })
        assertTrue("Should contain Loaded Baked Potatoes", titles.any { it.contains("Loaded Baked Potatoes") })
        assertTrue("Should contain Mujadara", titles.any { it.contains("Mujadara") })
    }

    @Test
    fun testToggleFavorite() {
        val firstRecipe = repository.recipes.value.first()
        val initialFavorite = firstRecipe.isFavorite

        repository.toggleRecipeFavorite(firstRecipe.id)
        val updatedFavorite = repository.recipes.value.first { it.id == firstRecipe.id }.isFavorite

        assertEquals(!initialFavorite, updatedFavorite)
    }

    @Test
    fun testGroceryOperations() {
        val initialCount = repository.groceryItems.value.size

        repository.addGroceryItem(
            name = "Test Apple",
            category = GroceryCategory.PRODUCE,
            amount = 3.0,
            unit = "pcs",
            price = 1.99
        )

        val newCount = repository.groceryItems.value.size
        assertEquals(initialCount + 1, newCount)

        val addedItem = repository.groceryItems.value.last()
        assertEquals("Test Apple", addedItem.name)

        repository.toggleGroceryChecked(addedItem.id)
        assertTrue(repository.groceryItems.value.first { it.id == addedItem.id }.isChecked)

        repository.removeGroceryItem(addedItem.id)
        assertEquals(initialCount, repository.groceryItems.value.size)
    }

    @Test
    fun testKitchenOperations() {
        val initialCount = repository.kitchenItems.value.size

        repository.addKitchenItem(
            name = "Test Milk",
            category = GroceryCategory.DAIRY,
            location = StorageLocation.FRIDGE,
            quantity = 1.0,
            unit = "carton",
            expiryDays = 7
        )

        assertEquals(initialCount + 1, repository.kitchenItems.value.size)
        val addedItem = repository.kitchenItems.value.last()
        assertEquals("Test Milk", addedItem.name)
        assertEquals(StorageLocation.FRIDGE, addedItem.location)
    }

    @Test
    fun testMealPlanSwapOperations() {
        val initialMealPlan = repository.mealPlan.value
        assertTrue(initialMealPlan.isNotEmpty())

        val firstMealItem = initialMealPlan.first()
        val candidateRecipes = repository.recipes.value.filter { it.id != firstMealItem.recipe.id }
        assertTrue(candidateRecipes.isNotEmpty())

        val targetRecipe = candidateRecipes.first()
        repository.swapMealPlanRecipe(firstMealItem.id, targetRecipe)

        val updatedMealItem = repository.mealPlan.value.first { it.id == firstMealItem.id }
        assertEquals(targetRecipe.id, updatedMealItem.recipe.id)

        repository.swapMealPlanItemWithNext(firstMealItem.id)
        val swappedAgain = repository.mealPlan.value.first { it.id == firstMealItem.id }
        assertNotNull(swappedAgain.recipe)
    }

    @Test
    fun testGenerateOrImportRecipeDynamicImages() {
        val initialCount = repository.recipes.value.size
        repository.generateOrImportRecipe("Crispy Honey Garlic Chicken")
        repository.generateOrImportRecipe("Creamy Tomato Basil Pasta")
        repository.generateOrImportRecipe("Fresh Greek Quinoa Salad")

        val recipes = repository.recipes.value
        assertEquals(initialCount + 3, recipes.size)

        val generatedRecipes = recipes.take(3)
        val imageUrls = generatedRecipes.map { it.imageUrl }.toSet()
        assertTrue("Generated/imported recipes should have diverse image URLs, not identical ones", imageUrls.size > 1)
    }

    @Test
    fun testGenerateOrImportRecipePizzaImage() {
        repository.generateOrImportRecipe("Homade pizza")
        val recipe = repository.recipes.value.first { it.title.contains("Homade pizza", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800", recipe.imageUrl)
    }

    @Test
    fun testGenerateOrImportRecipeHomemadePizzaImage() {
        repository.generateOrImportRecipe("Homemade Pizza")
        val recipe = repository.recipes.value.first { it.title.contains("Homemade Pizza", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800", recipe.imageUrl)
    }

    @Test
    fun testGenerateOrImportRecipeJustPizzaImage() {
        repository.generateOrImportRecipe("Pepperoni Pizza")
        val recipe = repository.recipes.value.first { it.title.contains("Pepperoni Pizza", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800", recipe.imageUrl)
    }

    @Test
    fun testApplePieImageAndIngredients() {
        repository.generateOrImportRecipe("Apple pie")
        val recipe = repository.recipes.value.first { it.title.contains("Apple pie", ignoreCase = true) }

        assertEquals("https://images.unsplash.com/photo-1568571780765-9276ac8b75a2?w=800", recipe.imageUrl)

        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Apples", ingredientNames.contains("Apples"))
        assertTrue("Ingredients should contain Cinnamon", ingredientNames.contains("Cinnamon"))
        assertTrue("Ingredients should contain Pie crust", ingredientNames.contains("Pie crust"))
    }

    @Test
    fun testGenerateOrImportRecipeLemonBarImage() {
        repository.generateOrImportRecipe("Lemon Bars")
        val recipe = repository.recipes.value.first { it.title.contains("Lemon Bars", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=800", recipe.imageUrl)
    }

    @Test
    fun testGenerateOrImportRecipeSaladImage() {
        repository.generateOrImportRecipe("Fresh Cobb Salad")
        val recipe = repository.recipes.value.first { it.title.contains("Fresh Cobb Salad", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600", recipe.imageUrl)
    }

    @Test
    fun testGenerateOrImportRecipeMargaritaImage() {
        repository.generateOrImportRecipe("Classic Margarita")
        val recipe = repository.recipes.value.first { it.title.contains("Classic Margarita", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800", recipe.imageUrl)
    }

    @Test
    fun testGenerateOrImportRecipeTacoImage() {
        repository.generateOrImportRecipe("Street Tacos")
        val recipe = repository.recipes.value.first { it.title.contains("Street Tacos", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=600", recipe.imageUrl)
    }

    @Test
    fun testGenerateOrImportRecipePastaImage() {
        repository.generateOrImportRecipe("Spaghetti Bolognese")
        val recipe = repository.recipes.value.first { it.title.contains("Spaghetti Bolognese", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=800", recipe.imageUrl)
    }

    @Test
    fun testGermanChocolateCakeImageAndIngredients() {
        repository.generateOrImportRecipe("German chocolate cake")
        val recipe = repository.recipes.value.first { it.title.contains("German chocolate cake", ignoreCase = true) }

        // Verify image URL is chocolate cake URL
        assertEquals("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800", recipe.imageUrl)

        // Verify ingredients contain cake-related items
        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Flour", ingredientNames.contains("Flour"))
        assertTrue("Ingredients should contain Sugar", ingredientNames.contains("Sugar"))
        assertTrue("Ingredients should contain Cocoa powder", ingredientNames.contains("Cocoa powder"))
        assertTrue("Ingredients should contain Butter", ingredientNames.contains("Butter"))
        assertTrue("Ingredients should contain Eggs", ingredientNames.contains("Eggs"))
        assertTrue("Ingredients should contain Sweetened condensed milk", ingredientNames.contains("Sweetened condensed milk"))
        assertTrue("Ingredients should contain Pecans", ingredientNames.contains("Pecans"))
        assertTrue("Ingredients should contain Shredded coconut", ingredientNames.contains("Shredded coconut"))

        // Verify baking instructions
        assertTrue("Instructions should contain preheating oven", recipe.instructions.any { it.contains("350°F", ignoreCase = true) })
    }

    @Test
    fun testTailoredIngredientsForPizzaTacosPastaAndSalad() {
        // Pizza
        repository.generateOrImportRecipe("Pepperoni Pizza")
        val pizzaRecipe = repository.recipes.value.first { it.title.contains("Pepperoni Pizza", ignoreCase = true) }
        val pizzaIngredients = pizzaRecipe.ingredients.map { it.name }
        assertTrue("Pizza should contain Pizza dough", pizzaIngredients.contains("Pizza dough"))
        assertTrue("Pizza should contain Mozzarella cheese", pizzaIngredients.contains("Mozzarella cheese"))
        assertTrue("Pizza should contain Tomato sauce", pizzaIngredients.contains("Tomato sauce"))

        // Taco
        repository.generateOrImportRecipe("Street Tacos")
        val tacoRecipe = repository.recipes.value.first { it.title.contains("Street Tacos", ignoreCase = true) }
        val tacoIngredients = tacoRecipe.ingredients.map { it.name }
        assertTrue("Taco should contain Tortillas", tacoIngredients.contains("Tortillas"))
        assertTrue("Taco should contain Ground beef or chicken", tacoIngredients.contains("Ground beef or chicken"))
        assertTrue("Taco should contain Salsa", tacoIngredients.contains("Salsa"))

        // Pasta
        repository.generateOrImportRecipe("Spaghetti Bolognese")
        val pastaRecipe = repository.recipes.value.first { it.title.contains("Spaghetti Bolognese", ignoreCase = true) }
        val pastaIngredients = pastaRecipe.ingredients.map { it.name }
        assertTrue("Pasta should contain Spaghetti", pastaIngredients.any { it.contains("Spaghetti", ignoreCase = true) || it.contains("Pasta", ignoreCase = true) })
        assertTrue("Pasta should contain Garlic", pastaIngredients.any { it.contains("Garlic", ignoreCase = true) })
        assertTrue("Pasta should contain Marinara sauce", pastaIngredients.any { it.contains("Marinara", ignoreCase = true) })

        // Salad
        repository.generateOrImportRecipe("Fresh Cobb Salad")
        val saladRecipe = repository.recipes.value.first { it.title.contains("Fresh Cobb Salad", ignoreCase = true) }
        val saladIngredients = saladRecipe.ingredients.map { it.name }
        assertTrue("Salad should contain Mixed greens", saladIngredients.contains("Mixed greens"))
        assertTrue("Salad should contain Cherry tomatoes", saladIngredients.contains("Cherry tomatoes"))
        assertTrue("Salad should contain Cucumber", saladIngredients.contains("Cucumber"))
    }

    @Test
    fun testJuiceDoesNotReturnCocktailImage() {
        repository.generateOrImportRecipe("Fresh Orange Juice")
        val recipe = repository.recipes.value.first { it.title.contains("Fresh Orange Juice", ignoreCase = true) }
        assertFalse("Juice should not return cocktail image", recipe.imageUrl == "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800")
    }

    @Test
    fun testBlackenedSalmonRecipeIngredientsAndInstructions() {
        repository.generateOrImportRecipe("Blackened Salmon")
        val recipe = repository.recipes.value.first { it.title.contains("Blackened Salmon", ignoreCase = true) }

        // Verify image URL
        assertEquals("https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600", recipe.imageUrl)

        // Verify ingredients contain Salmon, Blackening Seasoning, and Lemon
        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Fresh Salmon Fillets", ingredientNames.any { it.contains("Salmon", ignoreCase = true) })
        assertTrue("Ingredients should contain Blackening Seasoning", ingredientNames.contains("Blackening Seasoning"))
        assertTrue("Ingredients should contain Fresh Lemon", ingredientNames.any { it.contains("Lemon", ignoreCase = true) })

        // Verify instructions contain steps 1-4
        assertTrue("Step 1 should mention pat dry and rub with blackening seasoning", recipe.instructions.any { it.contains("Pat salmon fillets dry", ignoreCase = true) && it.contains("blackening seasoning", ignoreCase = true) })
        assertTrue("Step 2 should mention skillet over medium-high heat", recipe.instructions.any { it.contains("skillet over medium-high heat", ignoreCase = true) })
        assertTrue("Step 3 should mention sear salmon for 3-4 minutes", recipe.instructions.any { it.contains("Sear salmon for 3-4 minutes", ignoreCase = true) })
        assertTrue("Step 4 should mention drizzle with fresh lemon juice", recipe.instructions.any { it.contains("fresh lemon juice", ignoreCase = true) })
    }

    @Test
    fun testExpandedTemplatesAndSmartFallback() {
        // Steak
        repository.generateOrImportRecipe("Ribeye Steak")
        val steakRecipe = repository.recipes.value.first { it.title.contains("Ribeye Steak", ignoreCase = true) }
        val steakIngredients = steakRecipe.ingredients.map { it.name }
        assertTrue("Steak should contain Ribeye Steak", steakIngredients.contains("Ribeye Steak"))
        assertTrue("Steak should contain Garlic Butter", steakIngredients.contains("Garlic Butter"))

        // Soup
        repository.generateOrImportRecipe("Vegetable Soup")
        val soupRecipe = repository.recipes.value.first { it.title.contains("Vegetable Soup", ignoreCase = true) }
        val soupIngredients = soupRecipe.ingredients.map { it.name }
        assertTrue("Soup should contain Vegetable Broth", soupIngredients.contains("Vegetable Broth"))

        // Burger
        repository.generateOrImportRecipe("Cheeseburger")
        val burgerRecipe = repository.recipes.value.first { it.title.contains("Cheeseburger", ignoreCase = true) }
        val burgerIngredients = burgerRecipe.ingredients.map { it.name }
        assertTrue("Burger should contain Ground Beef Patty", burgerIngredients.contains("Ground Beef Patty"))

        // Breakfast / Pancakes
        repository.generateOrImportRecipe("Fluffy Pancakes")
        val pancakeRecipe = repository.recipes.value.first { it.title.contains("Fluffy Pancakes", ignoreCase = true) }
        val pancakeIngredients = pancakeRecipe.ingredients.map { it.name }
        assertTrue("Pancakes should contain Pancake Mix", pancakeIngredients.contains("Pancake Mix"))

        // Curry
        repository.generateOrImportRecipe("Chicken Curry")
        val curryRecipe = repository.recipes.value.first { it.title.contains("Chicken Curry", ignoreCase = true) }
        val curryIngredients = curryRecipe.ingredients.map { it.name }
        assertTrue("Curry should contain Coconut Milk", curryIngredients.contains("Coconut Milk"))
        assertTrue("Curry should contain Curry Paste", curryIngredients.contains("Curry Paste"))

        // Smart Fallback
        repository.generateOrImportRecipe("Grilled Pork Chops")
        val fallbackRecipe = repository.recipes.value.first { it.title.contains("Grilled Pork Chops", ignoreCase = true) }
        val fallbackIngredients = fallbackRecipe.ingredients.map { it.name }
        assertTrue("Fallback main dish should be dynamic and contain 'Fresh Grilled Pork Chops'", fallbackIngredients.contains("Fresh Grilled Pork Chops"))
        assertFalse("Fallback should not contain generic 'Organic Protein Source'", fallbackIngredients.any { it.contains("Organic Protein", ignoreCase = true) })
    }

    @Test
    fun testImportSpaghettiPieUrlRecipe() {
        val url = "https://aspicyperspective.com/spaghetti-pie-recipe/"
        repository.generateOrImportRecipe(url)

        val recipe = repository.recipes.value.firstOrNull { it.title == "Spaghetti Pie Recipe" }
        assertNotNull("Recipe with extracted title 'Spaghetti Pie Recipe' should exist", recipe)
        recipe!!

        // Title is extracted as "Spaghetti Pie Recipe" (not "Imported from aspicyperspective.com")
        assertEquals("Spaghetti Pie Recipe", recipe.title)

        // Image is pasta/spaghetti (not salmon)
        assertEquals("https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=800", recipe.imageUrl)
        assertFalse("Image should not be salmon", recipe.imageUrl?.contains("photo-1519708227418") == true)

        // Ingredients contain Spaghetti, Marinara, Mozzarella, Eggs (not Apples, Cinnamon, Pie crust)
        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Spaghetti", ingredientNames.any { it.contains("Spaghetti", ignoreCase = true) })
        assertTrue("Ingredients should contain Marinara", ingredientNames.any { it.contains("Marinara", ignoreCase = true) })
        assertTrue("Ingredients should contain Mozzarella", ingredientNames.any { it.contains("Mozzarella", ignoreCase = true) })
        assertTrue("Ingredients should contain Eggs", ingredientNames.any { it.contains("Eggs", ignoreCase = true) })

        assertFalse("Ingredients should not contain Apples", ingredientNames.any { it.equals("Apples", ignoreCase = true) })
        assertFalse("Ingredients should not contain Cinnamon", ingredientNames.any { it.equals("Cinnamon", ignoreCase = true) })
        assertFalse("Ingredients should not contain Pie crust", ingredientNames.any { it.equals("Pie crust", ignoreCase = true) })
    }

    @Test
    fun testSavoryPiesVsDessertPies() {
        repository.generateOrImportRecipe("Chicken Pot Pie")
        val potPieRecipe = repository.recipes.value.first { it.title.contains("Chicken Pot Pie", ignoreCase = true) }
        val potPieIngredients = potPieRecipe.ingredients.map { it.name }

        assertTrue("Chicken Pot Pie should contain Diced Chicken Breast", potPieIngredients.any { it.contains("Chicken", ignoreCase = true) })
        assertTrue("Chicken Pot Pie should contain Pie Crust", potPieIngredients.any { it.contains("Pie Crust", ignoreCase = true) })
        assertFalse("Chicken Pot Pie should not contain Apples", potPieIngredients.any { it.contains("Apples", ignoreCase = true) })
        assertFalse("Chicken Pot Pie should not contain Cinnamon", potPieIngredients.any { it.contains("Cinnamon", ignoreCase = true) })

        repository.generateOrImportRecipe("Shepherd's Pie")
        val shepherdRecipe = repository.recipes.value.first { it.title.contains("Shepherd's Pie", ignoreCase = true) }
        val shepherdIngredients = shepherdRecipe.ingredients.map { it.name }

        assertTrue("Shepherd's Pie should contain Ground Beef or Lamb", shepherdIngredients.any { it.contains("Beef", ignoreCase = true) || it.contains("Lamb", ignoreCase = true) })
        assertTrue("Shepherd's Pie should contain Mashed Potatoes", shepherdIngredients.any { it.contains("Mashed Potatoes", ignoreCase = true) })
        assertFalse("Shepherd's Pie should not contain Apples", shepherdIngredients.any { it.contains("Apples", ignoreCase = true) })
    }

    @Test
    fun testClassicPeanutButterCookiesImage() {
        repository.generateOrImportRecipe("Classic Peanut Butter Cookies")
        val recipe = repository.recipes.value.first { it.title.contains("Classic Peanut Butter Cookies", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=800", recipe.imageUrl)
    }

    @Test
    fun testExpandedKeywordMatchingImages() {
        val cookieUrl = "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=800"
        val breakfastUrl = "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=600"
        val burgerUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600"
        val dessertUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800"

        // Cookies / Peanut Butter / Sweets keywords
        listOf("Chocolate Chip Cookie", "Butter Cookies", "Peanut Biscuits", "Fudge Brownie", "Glazed Donut", "Blueberry Muffin").forEach { title ->
            repository.generateOrImportRecipe(title)
            val recipe = repository.recipes.value.first { it.title.contains(title, ignoreCase = true) }
            assertEquals("Expected cookie URL for '$title'", cookieUrl, recipe.imageUrl)
        }

        // Breakfast keywords: toast, egg, crepe, waffle, pancake
        listOf("Avocado Toast", "Fried Egg", "French Crepe", "Belgian Waffle", "Fluffy Pancake").forEach { title ->
            repository.generateOrImportRecipe(title)
            val recipe = repository.recipes.value.first { it.title.contains(title, ignoreCase = true) }
            assertEquals("Expected breakfast URL for '$title'", breakfastUrl, recipe.imageUrl)
        }

        // Sandwich keyword
        repository.generateOrImportRecipe("Club Sandwich")
        val sandwichRecipe = repository.recipes.value.first { it.title.contains("Club Sandwich", ignoreCase = true) }
        assertEquals("Expected burger/sandwich URL for 'Club Sandwich'", burgerUrl, sandwichRecipe.imageUrl)

        // Ice cream keyword
        repository.generateOrImportRecipe("Vanilla Ice Cream")
        val iceCreamRecipe = repository.recipes.value.first { it.title.contains("Vanilla Ice Cream", ignoreCase = true) }
        assertEquals("Expected dessert URL for 'Vanilla Ice Cream'", dessertUrl, iceCreamRecipe.imageUrl)
    }

    @Test
    fun testSanitizeEggToastUrlInRecipe() {
        val oldEggToastRecipe = Recipe(
            id = "test_egg_toast_1",
            title = "Classic Peanut Butter Cookies",
            description = "Test description",
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8",
            cookTimeMinutes = 10,
            prepTimeMinutes = 5,
            servings = 2,
            pricePerServing = 1.50,
            category = "Snacks",
            cuisine = "American",
            tags = listOf("Cookies"),
            ingredients = emptyList(),
            instructions = emptyList()
        )

        val sanitized = repository.sanitizeRecipe(oldEggToastRecipe)
        assertEquals("https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=800", sanitized.imageUrl)
    }

    @Test
    fun testGetRecipeByIdSanitizesEggToastUrl() {
        val oldRecipe = Recipe(
            id = "test_egg_toast_2",
            title = "Avocado Toast",
            description = "Toast description",
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8",
            cookTimeMinutes = 5,
            prepTimeMinutes = 5,
            servings = 1,
            pricePerServing = 2.00,
            category = "Breakfast",
            cuisine = "American",
            tags = listOf("Breakfast"),
            ingredients = emptyList(),
            instructions = emptyList()
        )

        repository.swapMealPlanRecipe("m_1", oldRecipe)
        repository.generateOrImportRecipe("Avocado Toast")
        val recipeInRepo = repository.recipes.value.first { it.title == "Avocado Toast" }
        assertFalse("Repository should not store egg toast URL", recipeInRepo.imageUrl?.contains("1525351484163") == true)

        val retrieved = repository.getRecipeById(recipeInRepo.id)
        assertNotNull(retrieved)
        assertFalse("getRecipeById should return sanitized image URL", retrieved?.imageUrl?.contains("1525351484163") == true)
    }

    @Test
    fun testPeanutButterCookieIngredientsAndInstructions() {
        repository.generateOrImportRecipe("Peanut Butter Cookies")
        val recipe = repository.recipes.value.first { it.title.contains("Peanut Butter Cookies", ignoreCase = true) }

        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Peanut Butter", ingredientNames.any { it.contains("Peanut Butter", ignoreCase = true) })
        assertTrue("Ingredients should contain Sugar", ingredientNames.any { it.contains("Sugar", ignoreCase = true) })
        assertTrue("Ingredients should contain Flour", ingredientNames.any { it.contains("Flour", ignoreCase = true) })
        assertTrue("Ingredients should contain Butter", ingredientNames.any { it.contains("Butter", ignoreCase = true) })
        assertTrue("Ingredients should contain Egg", ingredientNames.any { it.contains("Egg", ignoreCase = true) })

        assertTrue("Instructions should mention crisscross fork pattern", recipe.instructions.any { it.contains("fork", ignoreCase = true) && it.contains("crisscross", ignoreCase = true) })
    }

    @Test
    fun testPeanutButterCookiesDoesNotMatchGarlicButter() {
        repository.generateOrImportRecipe("Classic Peanut Butter Cookies")
        val recipe = repository.recipes.value.first { it.title.contains("Classic Peanut Butter Cookies", ignoreCase = true) }

        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Peanut Butter", ingredientNames.any { it.contains("Peanut Butter", ignoreCase = true) })
        assertTrue("Ingredients should contain Sugar", ingredientNames.any { it.contains("Sugar", ignoreCase = true) })
        assertTrue("Ingredients should contain Flour", ingredientNames.any { it.contains("Flour", ignoreCase = true) })
        assertTrue("Ingredients should contain Egg", ingredientNames.any { it.contains("Egg", ignoreCase = true) })
        assertTrue("Ingredients should contain Butter", ingredientNames.any { it.contains("Butter", ignoreCase = true) })

        assertFalse("Ingredients should NOT contain Salmon", ingredientNames.any { it.contains("Salmon", ignoreCase = true) })
        assertFalse("Ingredients should NOT contain Garlic", ingredientNames.any { it.contains("Garlic", ignoreCase = true) })
        assertFalse("Ingredients should NOT contain Olive Oil", ingredientNames.any { it.contains("Olive Oil", ignoreCase = true) })
    }

    @Test
    fun testChickenDumplingsImageAndIngredients() {
        repository.generateOrImportRecipe("Chicken dumplings")
        val recipe = repository.recipes.value.first { it.title.contains("Chicken dumplings", ignoreCase = true) }

        // Verify image URL
        assertEquals("https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800", recipe.imageUrl)

        // Verify ingredients contain ground chicken, dumpling wrappers, soy sauce, and ginger
        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Ground Chicken", ingredientNames.contains("Ground Chicken"))
        assertTrue("Ingredients should contain Dumpling Wrappers / Dough", ingredientNames.contains("Dumpling Wrappers / Dough"))
        assertTrue("Ingredients should contain Soy Sauce", ingredientNames.contains("Soy Sauce"))
        assertTrue("Ingredients should contain Fresh Ginger", ingredientNames.contains("Fresh Ginger"))
    }

    @Test
    fun testChickenDunmplingsTypoMatching() {
        val typos = listOf("dunmpling", "dunmplings", "dumplin", "dumplins", "potsticker", "gyoza", "dim sum")
        for (typo in typos) {
            val imageUrl = repository.getRecipeImageUrl("Chicken $typo")
            assertEquals("https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800", imageUrl)
        }

        repository.generateOrImportRecipe("Chicken dunmplings")
        val recipe = repository.recipes.value.first { it.title.equals("Chicken dunmplings", ignoreCase = true) }

        // Verify image URL matches steamed dumplings
        assertEquals("https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800", recipe.imageUrl)

        // Verify ground chicken dumpling ingredients are assigned
        val ingredientNames = recipe.ingredients.map { it.name }
        assertTrue("Ingredients should contain Ground Chicken", ingredientNames.contains("Ground Chicken"))
        assertTrue("Ingredients should contain Dumpling Wrappers / Dough", ingredientNames.contains("Dumpling Wrappers / Dough"))
        assertTrue("Ingredients should contain Soy Sauce", ingredientNames.contains("Soy Sauce"))
        assertTrue("Ingredients should contain Fresh Ginger", ingredientNames.contains("Fresh Ginger"))
    }

    @Test
    fun testLemonGarlicChickenImage() {
        assertEquals("https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600", repository.getRecipeImageUrl("Lemon Garlic Chicken"))
        assertEquals("https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600", repository.getRecipeImageUrl("Lemon Chicken"))

        repository.generateOrImportRecipe("Lemon Garlic Chicken")
        val recipe = repository.recipes.value.first { it.title.equals("Lemon Garlic Chicken", ignoreCase = true) }
        assertEquals("https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600", recipe.imageUrl)

        val sampleRecipe = repository.recipes.value.first { it.title.contains("Lemon Garlic Chicken", ignoreCase = true) && it.id == "rec_1" }
        assertEquals("https://images.unsplash.com/photo-1532550907401-a500c9a57435?w=600", sampleRecipe.imageUrl)
    }

    @Test
    fun testSouthernChickenAndDumplingsVsAsianGyoza() {
        // 1. Verify Southern Chicken and Dumplings keywords image URLs
        val southernKeywords = listOf(
            "chicken and dumplings",
            "chicken & dumplings",
            "cracker barrel",
            "dumpling stew",
            "dumpling soup"
        )
        for (kw in southernKeywords) {
            val imageUrl = repository.getRecipeImageUrl("Special $kw")
            assertEquals("https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800", imageUrl)
        }

        // 2. Generate "Chicken and dumplings" and verify image & biscuit dumplings recipe
        repository.generateOrImportRecipe("Chicken and dumplings")
        val southernRecipe = repository.recipes.value.first { it.title.equals("Chicken and dumplings", ignoreCase = true) }

        assertEquals("https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800", southernRecipe.imageUrl)

        val southernIngredients = southernRecipe.ingredients.map { it.name }
        assertTrue("Should contain Shredded Chicken Breast", southernIngredients.contains("Shredded Chicken Breast"))
        assertTrue("Should contain Chicken Broth", southernIngredients.contains("Chicken Broth"))
        assertTrue("Should contain All-Purpose Flour", southernIngredients.contains("All-Purpose Flour"))
        assertTrue("Should contain Baking Powder", southernIngredients.contains("Baking Powder"))
        assertTrue("Should contain Heavy Cream or Milk", southernIngredients.contains("Heavy Cream or Milk"))
        assertTrue("Should contain Butter", southernIngredients.contains("Butter"))
        assertTrue("Should contain Diced Carrots & Celery", southernIngredients.contains("Diced Carrots & Celery"))
        assertTrue("Should contain Onions & Garlic", southernIngredients.contains("Onions & Garlic"))

        assertTrue("Instructions should mention drop-dumpling dough", southernRecipe.instructions.any { it.contains("drop-dumpling dough", ignoreCase = true) })
        assertTrue("Instructions should mention simmering in rich chicken broth", southernRecipe.instructions.any { it.contains("rich chicken broth", ignoreCase = true) || it.contains("simmering chicken stew", ignoreCase = true) })

        // 3. Verify Asian Dumpling keywords image URLs
        val asianKeywords = listOf(
            "potsticker",
            "gyoza",
            "dim sum",
            "asian dumpling",
            "pan-fried dumpling"
        )
        for (kw in asianKeywords) {
            val imageUrl = repository.getRecipeImageUrl("Delicious $kw")
            assertEquals("https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800", imageUrl)
        }

        // 4. Generate "Pork gyoza" and verify Asian dumpling image & potstickers recipe
        repository.generateOrImportRecipe("Pork gyoza")
        val gyozaRecipe = repository.recipes.value.first { it.title.equals("Pork gyoza", ignoreCase = true) }

        assertEquals("https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800", gyozaRecipe.imageUrl)

        val gyozaIngredients = gyozaRecipe.ingredients.map { it.name }
        assertTrue("Should contain Dumpling Wrappers / Dough", gyozaIngredients.contains("Dumpling Wrappers / Dough"))
        assertTrue("Should contain Soy Sauce", gyozaIngredients.contains("Soy Sauce"))
        assertTrue("Should contain Fresh Ginger", gyozaIngredients.contains("Fresh Ginger"))

        assertTrue("Instructions should mention dumpling wrapper", gyozaRecipe.instructions.any { it.contains("dumpling wrapper", ignoreCase = true) })
    }

    @Test
    fun testGetUpcomingWeekRanges() {
        val ranges = repository.getUpcomingWeekRanges()
        assertEquals(4, ranges.size)
        ranges.forEach { range ->
            assertTrue("Range should not be empty", range.isNotBlank())
            assertTrue("Range should contain en-dash", range.contains("–"))
        }

        val options = repository.getWeekOptions()
        assertEquals(4, options.size)
        assertEquals("This week", options[0])
    }

    @Test
    fun testZipCodeStorage() {
        repository.setZipCode("90210")
        assertEquals("90210", repository.userProfile.value.zipCode)

        repository.setZipCode("10001")
        assertEquals("10001", repository.userProfile.value.zipCode)
    }

    @Test
    fun testIngredientExclusionsFiltering() {
        repository.addExcludedIngredient("corn")
        repository.addExcludedIngredient("peanuts")

        val exclusions = repository.userProfile.value.excludedIngredients
        assertTrue("Excluded ingredients should contain 'corn'", exclusions.contains("corn"))
        assertTrue("Excluded ingredients should contain 'peanuts'", exclusions.contains("peanuts"))

        repository.generateCostOptimizedPlan()
        val plan = repository.mealPlan.value
        plan.forEach { item ->
            val ingNames = item.recipe.ingredients.map { it.name.lowercase() }
            assertFalse("Planned recipes should omit excluded ingredient 'corn'", ingNames.any { it.contains("corn") })
            assertFalse("Planned recipes should omit excluded ingredient 'peanuts'", ingNames.any { it.contains("peanuts") })
        }

        repository.removeExcludedIngredient("corn")
        assertFalse("Excluded ingredients should no longer contain 'corn'", repository.userProfile.value.excludedIngredients.contains("corn"))
    }

    @Test
    fun testSmartRemindersGeneration() {
        val reminders = repository.getSmartReminders()
        assertEquals(3, reminders.size)

        val thawRem = reminders.first { it.type == com.package1.shopcook.model.ReminderType.THAW }
        val shopRem = reminders.first { it.type == com.package1.shopcook.model.ReminderType.SHOPPING_DAY }
        val expRem = reminders.first { it.type == com.package1.shopcook.model.ReminderType.EXPIRATION }

        assertTrue("Thaw reminder description should contain thaw text", thawRem.description.contains("Take out", ignoreCase = true) || thawRem.description.contains("Chicken Broth", ignoreCase = true))
        assertTrue("Shopping reminder description should contain items count", shopRem.description.contains("items on your list", ignoreCase = true))
        assertTrue("Expiration alert description should contain expires text", expRem.description.contains("expires", ignoreCase = true))
    }

    @Test
    fun testHouseholdShareCodeAndSync() {
        val newCode = repository.generateNewShareCode()
        assertTrue("Generated share code should start with YUM-", newCode.startsWith("YUM-"))
        assertEquals(newCode, repository.userProfile.value.shareCode)

        repository.joinHousehold("YUM-9999")
        assertEquals("YUM-9999", repository.userProfile.value.joinedShareCode)

        repository.leaveHousehold()
        assertEquals(null, repository.userProfile.value.joinedShareCode)
    }
}




