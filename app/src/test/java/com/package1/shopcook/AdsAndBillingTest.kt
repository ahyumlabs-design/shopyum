package com.package1.shopcook

import android.content.Context
import android.content.ContextWrapper
import com.package1.shopcook.ads.AdsManager
import com.package1.shopcook.billing.BillingManager
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.BillingState
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.model.UserTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdsAndBillingTest {

    private class TestContext : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }

    private lateinit var repository: ShopCookRepository

    @Before
    fun setUp() {
        repository = ShopCookRepository.instance
        repository.setUserTier(UserTier.FREE)
        AdsManager.resetCooldown()
        AdsManager.billingStateSupplier = null
    }

    @Test
    fun testUserTierAndBillingStateIsAdFree() {
        assertFalse("FREE tier must not be ad-free", UserTier.FREE.isAdFree)
        assertTrue("PRO tier must be ad-free", UserTier.PRO.isAdFree)
        assertTrue("LIFETIME tier must be ad-free", UserTier.LIFETIME.isAdFree)

        assertFalse("FREE BillingState must not be ad-free", BillingState.FREE.isAdFree)
        assertTrue("PRO BillingState must be ad-free", BillingState.PRO.isAdFree)
        assertTrue("LIFETIME BillingState must be ad-free", BillingState.LIFETIME.isAdFree)
    }

    @Test
    fun testUserProfileBillingStateMapping() {
        val freeProfile = UserProfile(userTier = UserTier.FREE)
        assertEquals(BillingState.FREE, freeProfile.billingState)
        assertFalse(freeProfile.isAdFree)
        assertFalse(freeProfile.isProUser)

        val proProfile = UserProfile(userTier = UserTier.PRO)
        assertEquals(BillingState.PRO, proProfile.billingState)
        assertTrue(proProfile.isAdFree)
        assertTrue(proProfile.isProUser)

        val lifetimeProfile = UserProfile(userTier = UserTier.LIFETIME)
        assertEquals(BillingState.LIFETIME, lifetimeProfile.billingState)
        assertTrue(lifetimeProfile.isAdFree)
        assertTrue(lifetimeProfile.isProUser)
    }

    @Test
    fun testRepositorySetUserTierAndBillingState() {
        repository.setUserTier(UserTier.PRO)
        assertEquals(UserTier.PRO, repository.userProfile.value.userTier)
        assertEquals(BillingState.PRO, repository.userProfile.value.billingState)
        assertTrue(repository.userProfile.value.isAdFree)

        repository.setUserTier(UserTier.LIFETIME)
        assertEquals(UserTier.LIFETIME, repository.userProfile.value.userTier)
        assertEquals(BillingState.LIFETIME, repository.userProfile.value.billingState)
        assertTrue(repository.userProfile.value.isAdFree)

        repository.setBillingState(BillingState.FREE)
        assertEquals(UserTier.FREE, repository.userProfile.value.userTier)
        assertEquals(BillingState.FREE, repository.userProfile.value.billingState)
        assertFalse(repository.userProfile.value.isAdFree)
    }

    @Test
    fun testAdsManagerAdMobTestUnitIds() {
        assertEquals("ca-app-pub-3940256099942544~3347511313", AdsManager.TEST_APP_ID)
        assertEquals("ca-app-pub-3940256099942544/6300978111", AdsManager.TEST_BANNER_AD_UNIT_ID)
        assertEquals("ca-app-pub-3940256099942544/1033173712", AdsManager.TEST_INTERSTITIAL_AD_UNIT_ID)
    }

    @Test
    fun testShowInterstitialWithUserTier() {
        val dummyContext = TestContext()

        // Test with PRO UserTier -> Ad suppressed
        var proCallbackExecuted = false
        AdsManager.showInterstitial(dummyContext, UserTier.PRO) {
            proCallbackExecuted = true
        }
        assertTrue("Callback must execute immediately for PRO user", proCallbackExecuted)
        assertFalse("canShowInterstitial must be false for PRO", AdsManager.canShowInterstitial(UserTier.PRO))

        // Test with LIFETIME UserTier -> Ad suppressed
        var lifetimeCallbackExecuted = false
        AdsManager.showInterstitial(dummyContext, UserTier.LIFETIME) {
            lifetimeCallbackExecuted = true
        }
        assertTrue("Callback must execute immediately for LIFETIME user", lifetimeCallbackExecuted)
        assertFalse("canShowInterstitial must be false for LIFETIME", AdsManager.canShowInterstitial(UserTier.LIFETIME))

        // Test with FREE UserTier -> Ad shown
        AdsManager.resetCooldown()
        assertTrue("canShowInterstitial must be true for FREE user before cooldown", AdsManager.canShowInterstitial(UserTier.FREE))

        var freeCallbackExecuted = false
        AdsManager.showInterstitial(dummyContext, UserTier.FREE) {
            freeCallbackExecuted = true
        }
        assertTrue("Callback must execute for FREE user", freeCallbackExecuted)
        assertFalse("canShowInterstitial must be false immediately after showing", AdsManager.canShowInterstitial(UserTier.FREE))
    }

    @Test
    fun testAdsManagerFrequencyCappingAndAdFreeSuppression() {
        val dummyContext = TestContext()

        // 1. Pro / AdFree user: Ad must NEVER be shown
        repository.setUserTier(UserTier.PRO)
        var callbackExecuted = false
        AdsManager.showInterstitialAd(dummyContext, isAdFreeUser = true) {
            callbackExecuted = true
        }
        assertTrue("Callback must execute immediately for ad-free user", callbackExecuted)
        assertFalse("canShowInterstitial must return false for ad-free user", AdsManager.canShowInterstitial(isAdFreeUser = true))

        // 2. Free user: initial show
        repository.setUserTier(UserTier.FREE)
        AdsManager.resetCooldown()
        assertTrue("Free user should be eligible for interstitial before cooldown", AdsManager.canShowInterstitial(isAdFreeUser = false))

        var adClosedCalled = false
        AdsManager.showInterstitialAd(dummyContext, isAdFreeUser = false) {
            adClosedCalled = true
        }
        assertTrue("onAdClosed callback must be invoked", adClosedCalled)

        // Immediately after showing, frequency cap should prevent showing another interstitial
        assertFalse("Should be on cooldown immediately after showing interstitial", AdsManager.canShowInterstitial(isAdFreeUser = false))

        // Second show call within cooldown period
        var secondCallbackExecuted = false
        AdsManager.showInterstitialAd(dummyContext, isAdFreeUser = false) {
            secondCallbackExecuted = true
        }
        assertTrue("Callback must be executed even when on cooldown", secondCallbackExecuted)

        // Reset cooldown allows showing again
        AdsManager.resetCooldown()
        assertTrue("After resetting cooldown, free user can show interstitial again", AdsManager.canShowInterstitial(isAdFreeUser = false))
    }

    @Test
    fun testBillingManagerProductIdsAndSimulation() {
        assertEquals("shop_yum_monthly_299", BillingManager.MONTHLY_PRODUCT_ID)
        assertEquals("shop_yum_yearly_1999", BillingManager.YEARLY_PRODUCT_ID)
        assertEquals("shop_yum_lifetime_3999", BillingManager.LIFETIME_PRODUCT_ID)

        val billingManager = BillingManager.getInstance()
        billingManager.simulateSuccess(UserTier.PRO)
        assertEquals(UserTier.PRO, repository.userProfile.value.userTier)
        assertEquals(BillingState.PRO, billingManager.billingState.value)

        billingManager.simulateSuccess(UserTier.LIFETIME)
        assertEquals(UserTier.LIFETIME, repository.userProfile.value.userTier)
        assertEquals(BillingState.LIFETIME, billingManager.billingState.value)

        billingManager.simulateSuccess(UserTier.FREE)
        assertEquals(UserTier.FREE, repository.userProfile.value.userTier)
        assertEquals(BillingState.FREE, billingManager.billingState.value)
    }
}
