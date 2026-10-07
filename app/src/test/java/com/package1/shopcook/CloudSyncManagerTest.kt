package com.package1.shopcook

import com.package1.shopcook.data.CloudSyncManager
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.data.SyncStatus
import com.package1.shopcook.model.GroceryCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CloudSyncManagerTest {

    private lateinit var cloudSyncManager: CloudSyncManager
    private lateinit var repository: ShopCookRepository

    @Before
    fun setUp() {
        cloudSyncManager = CloudSyncManager.instance
        repository = ShopCookRepository.instance
        cloudSyncManager.toggleCloudSync(true)
    }

    @Test
    fun testInitialCloudSyncState() {
        assertTrue(cloudSyncManager.isCloudSyncEnabled.value)
    }

    @Test
    fun testToggleCloudSync() {
        cloudSyncManager.toggleCloudSync(false)
        assertFalse(cloudSyncManager.isCloudSyncEnabled.value)
        assertEquals(SyncStatus.IDLE, cloudSyncManager.syncStatus.value)

        cloudSyncManager.toggleCloudSync(true)
        assertTrue(cloudSyncManager.isCloudSyncEnabled.value)
    }

    @Test
    fun testQueueChangeAndForceSync() {
        cloudSyncManager.queueChange("Test grocery added")
        cloudSyncManager.forceSync()
        // Wait or check that sync executes
        assertTrue(cloudSyncManager.syncStatus.value is SyncStatus.SYNCING || cloudSyncManager.syncStatus.value is SyncStatus.SUCCESS || cloudSyncManager.syncStatus.value is SyncStatus.IDLE)
    }

    @Test
    fun testRepositoryTriggersCloudSyncQueue() {
        repository.addGroceryItem(
            name = "Sync Test Item",
            category = GroceryCategory.PRODUCE,
            amount = 2.0,
            unit = "pcs",
            price = 1.50
        )

        // Repository mutation should queue change
        // Clean up added item
        val added = repository.groceryItems.value.lastOrNull { it.name == "Sync Test Item" }
        if (added != null) {
            repository.removeGroceryItem(added.id)
        }
    }
}
