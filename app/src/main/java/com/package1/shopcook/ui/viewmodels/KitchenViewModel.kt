package com.package1.shopcook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.KitchenItem
import com.package1.shopcook.model.Recipe
import com.package1.shopcook.model.StorageLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KitchenViewModel(
    private val repository: ShopCookRepository = ShopCookRepository.instance
) : ViewModel() {

    val kitchenItems: StateFlow<List<KitchenItem>> = repository.kitchenItems
    val recipes: StateFlow<List<Recipe>> = repository.recipes

    fun addItem(
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
        viewModelScope.launch(Dispatchers.IO) {
            repository.addKitchenItem(name, category, location, quantity, unit, expiryDays, isLeftover, leftoverServings, isAlreadyHave)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeKitchenItem(itemId)
        }
    }

    fun toggleAlreadyHave(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleKitchenItemAlreadyHave(itemId)
        }
    }

    fun getSmartReminders(): List<com.package1.shopcook.model.SmartReminder> {
        return repository.getSmartReminders()
    }
}