package com.package1.shopcook.data

import com.package1.shopcook.model.GroceryItem
import com.package1.shopcook.model.KitchenItem
import com.package1.shopcook.model.MealPlanItem
import com.package1.shopcook.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SyncStatus {
    object IDLE : SyncStatus()
    object SYNCING : SyncStatus()
    object SUCCESS : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}

class CloudSyncManager private constructor() {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isCloudSyncEnabled = MutableStateFlow(true)
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    private val _pendingQueue = MutableStateFlow<List<String>>(emptyList())
    val pendingQueue: StateFlow<List<String>> = _pendingQueue.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _shareCode = MutableStateFlow("YUM-8924")
    val shareCode: StateFlow<String> = _shareCode.asStateFlow()

    private val _joinedShareCode = MutableStateFlow<String?>(null)
    val joinedShareCode: StateFlow<String?> = _joinedShareCode.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun toggleCloudSync(enabled: Boolean) {
        _isCloudSyncEnabled.value = enabled
        if (enabled) {
            triggerSync()
        } else {
            _syncStatus.value = SyncStatus.IDLE
        }
    }

    fun queueChange(changeDescription: String) {
        if (!_isCloudSyncEnabled.value) return
        _pendingQueue.update { it + changeDescription }
        triggerSync()
    }

    fun triggerSync() {
        if (!_isCloudSyncEnabled.value) return
        if (_syncStatus.value is SyncStatus.SYNCING) return

        scope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            try {
                // Simulate network sync with local-first offline support
                delay(600)
                _pendingQueue.value = emptyList()
                _lastSyncTime.value = System.currentTimeMillis()
                _syncStatus.value = SyncStatus.SUCCESS
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR(e.localizedMessage ?: "Sync failed")
            }
        }
    }

    fun forceSync() {
        triggerSync()
    }

    fun setShareCode(code: String) {
        _shareCode.value = code
    }

    fun joinHousehold(code: String) {
        val cleanCode = code.trim().uppercase()
        _joinedShareCode.value = cleanCode
        queueChange("Joined household $cleanCode")
        triggerSync()
    }

    fun leaveHousehold() {
        val previous = _joinedShareCode.value
        _joinedShareCode.value = null
        if (previous != null) {
            queueChange("Left household $previous")
            triggerSync()
        }
    }

    fun generateNewShareCode(): String {
        val randomNum = (1000..9999).random()
        val newCode = "YUM-$randomNum"
        _shareCode.value = newCode
        queueChange("Generated new share code $newCode")
        triggerSync()
        return newCode
    }

    fun syncMealPlans(mealPlans: List<MealPlanItem>) {
        val activeCode = _joinedShareCode.value ?: _shareCode.value
        queueChange("Sync meal plans (${mealPlans.size} items) [Household: $activeCode]")
    }

    fun syncRecipes(recipes: List<Recipe>) {
        val activeCode = _joinedShareCode.value ?: _shareCode.value
        queueChange("Sync recipes (${recipes.size} items) [Household: $activeCode]")
    }

    fun syncGroceryItems(groceryItems: List<GroceryItem>) {
        val activeCode = _joinedShareCode.value ?: _shareCode.value
        queueChange("Sync grocery items (${groceryItems.size} items) [Household: $activeCode]")
    }

    fun syncPantryInventory(kitchenItems: List<KitchenItem>) {
        val activeCode = _joinedShareCode.value ?: _shareCode.value
        queueChange("Sync pantry inventory (${kitchenItems.size} items) [Household: $activeCode]")
    }

    companion object {
        val instance: CloudSyncManager by lazy { CloudSyncManager() }
    }
}
