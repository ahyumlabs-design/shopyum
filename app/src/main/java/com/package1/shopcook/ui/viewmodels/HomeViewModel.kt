package com.package1.shopcook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.MealPlanItem
import com.package1.shopcook.model.Recipe
import com.package1.shopcook.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ShopCookRepository = ShopCookRepository.instance
) : ViewModel() {

    val recipes: StateFlow<List<Recipe>> = repository.recipes
    val mealPlan: StateFlow<List<MealPlanItem>> = repository.mealPlan
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    private val _selectedWeek = MutableStateFlow("This week")
    val selectedWeek: StateFlow<String> = _selectedWeek.asStateFlow()

    fun getRecipeById(id: String): Recipe? {
        return repository.getRecipeById(id)
    }

    fun selectWeek(week: String) {
        _selectedWeek.value = week
        repository.setSelectedWeek(week)
    }

    fun swapMeal(mealId: String, newRecipe: Recipe? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            if (newRecipe != null) {
                repository.swapMealPlanRecipe(mealId, newRecipe)
            } else {
                repository.swapMealPlanItemWithNext(mealId)
            }
        }
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleRecipeFavorite(recipeId)
        }
    }

    fun addRecipeToGrocery(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecipeIngredientsToGrocery(recipe)
        }
    }

    fun toggleMealCooked(mealId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleMealCooked(mealId)
        }
    }

    fun generateOrImportRecipe(promptOrUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.generateOrImportRecipe(promptOrUrl)
        }
    }

    fun getSmartReminders(): List<com.package1.shopcook.model.SmartReminder> {
        return repository.getSmartReminders()
    }
}
