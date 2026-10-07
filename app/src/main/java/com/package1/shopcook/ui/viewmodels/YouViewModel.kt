package com.package1.shopcook.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.package1.shopcook.billing.BillingManager
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.data.SyncStatus
import com.package1.shopcook.model.BillingState
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.model.UserTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class YouViewModel(
    private val repository: ShopCookRepository = ShopCookRepository.instance
) : ViewModel() {

    private val billingManager: BillingManager = BillingManager.getInstance()
    val billingState: StateFlow<BillingState> = billingManager.billingState

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val syncStatus: StateFlow<SyncStatus> = repository.cloudSyncManager.syncStatus
    val isCloudSyncEnabled: StateFlow<Boolean> = repository.cloudSyncManager.isCloudSyncEnabled
    val pendingQueue: StateFlow<List<String>> = repository.cloudSyncManager.pendingQueue
    val lastSyncTime: StateFlow<Long> = repository.cloudSyncManager.lastSyncTime

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cloudSyncManager.toggleCloudSync(enabled)
        }
    }

    fun forceSync() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cloudSyncManager.forceSync()
        }
    }

    fun setUserTier(tier: UserTier) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setUserTier(tier)
        }
    }

    fun setBillingState(state: BillingState) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBillingState(state)
        }
    }

    fun toggleProUser() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleProUser()
        }
    }

    fun launchBilling(activity: Activity?, productId: String, fallbackTier: UserTier) {
        if (activity != null) {
            billingManager.launchBillingFlow(activity, productId, fallbackTier)
        } else {
            billingManager.simulateSuccess(fallbackTier)
        }
    }

    fun buyMonthly(activity: Activity?) {
        launchBilling(activity, BillingManager.MONTHLY_PRODUCT_ID, UserTier.PRO)
    }

    fun buyYearly(activity: Activity?) {
        launchBilling(activity, BillingManager.YEARLY_PRODUCT_ID, UserTier.PRO)
    }

    fun buyLifetime(activity: Activity?) {
        launchBilling(activity, BillingManager.LIFETIME_PRODUCT_ID, UserTier.LIFETIME)
    }

    fun setServingsCount(servings: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setServingsCount(servings)
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

    fun joinHousehold(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.joinHousehold(code)
        }
    }

    fun leaveHousehold() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.leaveHousehold()
        }
    }

    fun generateNewShareCode() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.generateNewShareCode()
        }
    }
}
