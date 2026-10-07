package com.package1.shopcook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.MealPlanItem
import com.package1.shopcook.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlanViewModel(
    private val repository: ShopCookRepository = ShopCookRepository.instance
) : ViewModel() {

    val mealPlan: StateFlow<List<MealPlanItem>> = repository.mealPlan
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    fun toggleMealCooked(mealId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleMealCooked(mealId)
        }
    }

    fun setServingsCount(servings: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setServingsCount(servings)
        }
    }

    fun setGroceryBudget(budget: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setGroceryBudget(budget)
        }
    }

    fun setActiveCookingDays(days: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setActiveCookingDays(days)
        }
    }

    fun toggleDietaryFilter(filter: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleDietaryFilter(filter)
        }
    }

    fun setIsCheapestWeek(cheapest: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setIsCheapestWeek(cheapest)
        }
    }

    fun selectWeek(week: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSelectedWeek(week)
        }
    }

    fun generateCostOptimizedPlan() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.generateCostOptimizedPlan()
        }
    }

    fun setSelectedStore(storeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSelectedStore(storeName)
        }
    }

    fun setZipCode(zipCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setZipCode(zipCode)
        }
    }

    fun addExcludedIngredient(ingredient: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addExcludedIngredient(ingredient)
        }
    }

    fun removeExcludedIngredient(ingredient: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeExcludedIngredient(ingredient)
        }
    }
}