package com.package1.shopcook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.GroceryItem
import com.package1.shopcook.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroceryViewModel(
    private val repository: ShopCookRepository = ShopCookRepository.instance
) : ViewModel() {

    val groceryItems: StateFlow<List<GroceryItem>> = repository.groceryItems
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    fun toggleChecked(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleGroceryChecked(itemId)
        }
    }

    fun addItem(name: String, category: GroceryCategory, amount: Double, unit: String, price: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGroceryItem(name, category, amount, unit, price)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeGroceryItem(itemId)
        }
    }

    fun clearChecked() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCheckedGroceryItems()
        }
    }

    fun setSelectedStore(storeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSelectedStore(storeName)
        }
    }
}