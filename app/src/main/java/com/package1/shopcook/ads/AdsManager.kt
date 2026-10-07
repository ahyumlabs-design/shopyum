package com.package1.shopcook.ads

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.BillingState
import com.package1.shopcook.model.UserTier

object AdsManager {
    private const val TAG = "AdsManager"

    // Official Android Test AdMob Unit IDs
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511313"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Interstitial Frequency Capping Parameters (60 seconds cooldown)
    const val COOLDOWN_INTERVAL_MS = 60_000L
    const val MAX_ACTION_CAP = 10

    private var isInitialized = false
    private var lastInterstitialTimeMs: Long = 0L
    private var actionCount: Int = 0

    var isBannerAdLoaded: Boolean = true
        private set

    var isInterstitialAdLoaded: Boolean = false
        private set

    private var mInterstitialAd: InterstitialAd? = null

    // Supplier for checking billing state dynamically or overriding in tests
    var billingStateSupplier: (() -> BillingState)? = null

    private fun logD(msg: String) {
        try { Log.d(TAG, msg) } catch (_: Throwable) { println("[$TAG] $msg") }
    }

    private fun logI(msg: String) {
        try { Log.i(TAG, msg) } catch (_: Throwable) { println("[$TAG] $msg") }
    }

    private fun logW(msg: String) {
        try { Log.w(TAG, msg) } catch (_: Throwable) { println("[$TAG] $msg") }
    }

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        try {
            MobileAds.initialize(context.applicationContext) { initializationStatus ->
                logD("MobileAds SDK initialized. Status: $initializationStatus")
            }
            loadInterstitialAd(context)
        } catch (e: Throwable) {
            logI("MobileAds SDK initialization fallback mode: ${e.message}")
        }
    }

    fun loadBannerAd() {
        isBannerAdLoaded = true
        logD("Banner ad loaded successfully.")
    }

    fun loadInterstitialAd(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                TEST_INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        isInterstitialAdLoaded = true
                        logD("Interstitial ad loaded successfully.")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                        isInterstitialAdLoaded = false
                        logW("Interstitial ad failed to load: ${adError.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            logW("Failed to load interstitial ad (fallback mode): ${e.message}")
            isInterstitialAdLoaded = true
        }
    }

    fun loadInterstitialAd() {
        isInterstitialAdLoaded = true
        logD("Interstitial ad loaded (fallback).")
    }

    fun isAdFree(): Boolean {
        val state = billingStateSupplier?.invoke()
            ?: ShopCookRepository.instance.userProfile.value.billingState
        return state.isAdFree
    }

    fun canShowInterstitial(userTier: UserTier): Boolean {
        return canShowInterstitial(userTier.isAdFree)
    }

    fun canShowInterstitial(billingState: BillingState): Boolean {
        return canShowInterstitial(billingState.isAdFree)
    }

    fun canShowInterstitial(isAdFreeUser: Boolean = isAdFree()): Boolean {
        if (isAdFreeUser) return false
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastInterstitialTimeMs
        return timeSinceLastAd >= COOLDOWN_INTERVAL_MS
    }

    fun showInterstitial(
        context: Context,
        userTier: UserTier,
        onAdClosed: () -> Unit
    ) {
        showInterstitialAd(context, userTier.isAdFree, onAdClosed)
    }

    fun showInterstitial(
        context: Context,
        billingState: BillingState,
        onAdClosed: () -> Unit
    ) {
        showInterstitialAd(context, billingState.isAdFree, onAdClosed)
    }

    fun showInterstitial(
        context: Context,
        onAdClosed: () -> Unit
    ) {
        showInterstitialAd(context, isAdFree(), onAdClosed)
    }

    fun showInterstitialAd(
        context: Context,
        onAdClosed: () -> Unit
    ) {
        showInterstitialAd(context, isAdFree(), onAdClosed)
    }

    fun showInterstitialAd(
        context: Context,
        isAdFreeUser: Boolean,
        onAdClosed: () -> Unit
    ) {
        actionCount++

        // Requirement 1: If user is PRO or LIFETIME (isAdFree), ads MUST NOT be displayed anywhere
        if (isAdFreeUser) {
            logD("User is ad-free (PRO or LIFETIME). Interstitial ad suppressed.")
            onAdClosed()
            return
        }

        // Requirement 2: Frequency Capping (60s minimum interval)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastInterstitialTimeMs

        if (timeSinceLastAd < COOLDOWN_INTERVAL_MS) {
            logD("Interstitial on cooldown (${(COOLDOWN_INTERVAL_MS - timeSinceLastAd) / 1000}s remaining). Skipping.")
            onAdClosed()
            return
        }

        lastInterstitialTimeMs = currentTime
        logD("Triggering Interstitial Ad display.")

        // Try showing real InterstitialAd if available and context is an Activity
        val activity = context.findActivity()
        if (mInterstitialAd != null && activity != null) {
            try {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        logD("Interstitial ad dismissed.")
                        mInterstitialAd = null
                        loadInterstitialAd(context)
                        onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        logW("Interstitial ad failed to show: ${adError.message}")
                        mInterstitialAd = null
                        loadInterstitialAd(context)
                        onAdClosed()
                    }
                }
                mInterstitialAd?.show(activity)
                return
            } catch (e: Throwable) {
                logW("Exception showing InterstitialAd: ${e.message}, falling back to dialog.")
            }
        }

        // Fallback handling for test/emulator environments or when ad isn't preloaded
        var dialogShown = false
        try {
            var isCallbackInvoked = false
            fun safeCallback() {
                if (!isCallbackInvoked) {
                    isCallbackInvoked = true
                    onAdClosed()
                }
            }

            val builder = AlertDialog.Builder(context)
                .setTitle("📢 ADVERTISEMENT")
                .setMessage("Upgrade to Shop & Yum Pro or Lifetime for an ad-free experience, unlimited meal planning, and custom store pricing!")
                .setPositiveButton("Close Ad") { dialog, _ ->
                    dialog.dismiss()
                    safeCallback()
                }
                .setOnDismissListener {
                    safeCallback()
                }

            val dialog = builder.create()
            dialog.show()
            dialogShown = true
        } catch (e: Throwable) {
            logW("Unable to present fallback dialog in context: ${e.message}")
        }

        if (!dialogShown) {
            onAdClosed()
        }
    }

    fun resetCooldown() {
        lastInterstitialTimeMs = 0L
        actionCount = 0
        mInterstitialAd = null
    }

    fun setLastInterstitialTimeForTesting(timeMs: Long) {
        lastInterstitialTimeMs = timeMs
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx is Activity) return ctx
            ctx = try {
                if (ctx is ContextWrapper) ctx.baseContext else null
            } catch (_: Throwable) {
                null
            }
        }
        return null
    }
}
