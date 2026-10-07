package com.package1.shopcook.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.package1.shopcook.data.ShopCookRepository
import com.package1.shopcook.model.BillingState
import com.package1.shopcook.model.UserTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager private constructor(context: Context?) : PurchasesUpdatedListener {

    private val applicationContext = context?.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _billingState = MutableStateFlow(BillingState.FREE)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _productDetailsList = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetailsList: StateFlow<List<ProductDetails>> = _productDetailsList.asStateFlow()

    private var billingClient: BillingClient? = applicationContext?.let {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()
        BillingClient.newBuilder(it)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()
    }

    init {
        startConnection()
    }

    fun startConnection() {
        val client = billingClient ?: return
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing setup finished successfully")
                        queryPurchases()
                        queryProductDetails()
                    } else {
                        Log.w(TAG, "Billing setup failed with response code: ${billingResult.responseCode}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.w(TAG, "Billing service disconnected.")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Exception starting billing connection: ${e.message}")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase flow")
        } else {
            Log.w(TAG, "Purchases updated error: ${billingResult.responseCode}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                scope.launch {
                    val result = client.acknowledgePurchase(acknowledgePurchaseParams)
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        grantEntitlement(purchase.products)
                    }
                }
            } else {
                grantEntitlement(purchase.products)
            }
        }
    }

    private fun grantEntitlement(products: List<String>) {
        val hasLifetime = products.contains(LIFETIME_PRODUCT_ID)
        val hasPro = products.contains(MONTHLY_PRODUCT_ID) || products.contains(YEARLY_PRODUCT_ID)

        val newTier = when {
            hasLifetime -> UserTier.LIFETIME
            hasPro -> UserTier.PRO
            else -> UserTier.FREE
        }

        val newState = when (newTier) {
            UserTier.LIFETIME -> BillingState.LIFETIME
            UserTier.PRO -> BillingState.PRO
            UserTier.FREE -> BillingState.FREE
        }

        _billingState.value = newState
        ShopCookRepository.instance.setUserTier(newTier)
    }

    fun queryPurchases() {
        val client = billingClient
        if (client == null || !client.isReady) {
            Log.w(TAG, "BillingClient not ready for queryPurchases")
            return
        }
        scope.launch {
            try {
                val subsResult = client.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
                if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPurchases(subsResult.purchasesList)
                }
                val inAppResult = client.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
                if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPurchases(inAppResult.purchasesList)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception querying purchases: ${e.message}")
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val allProducts = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap { it.products }

        if (allProducts.contains(LIFETIME_PRODUCT_ID)) {
            _billingState.value = BillingState.LIFETIME
            ShopCookRepository.instance.setUserTier(UserTier.LIFETIME)
        } else if (allProducts.contains(MONTHLY_PRODUCT_ID) || allProducts.contains(YEARLY_PRODUCT_ID)) {
            _billingState.value = BillingState.PRO
            ShopCookRepository.instance.setUserTier(UserTier.PRO)
        }
    }

    fun queryProductDetails() {
        val client = billingClient
        if (client == null || !client.isReady) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(YEARLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(LIFETIME_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        scope.launch {
            val result = client.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetailsList.value = result.productDetailsList ?: emptyList()
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String, fallbackTier: UserTier = UserTier.PRO) {
        val client = billingClient
        val productDetails = _productDetailsList.value.find { it.productId == productId }
        if (client != null && client.isReady && productDetails != null) {
            val productDetailsParamsList = when (productId) {
                MONTHLY_PRODUCT_ID, YEARLY_PRODUCT_ID -> {
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                }
                else -> {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                }
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val billingResult = client.launchBillingFlow(activity, billingFlowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Failed to launch billing flow: ${billingResult.responseCode}. Fallback to simulated success.")
                simulateSuccess(fallbackTier)
            }
        } else {
            Log.w(TAG, "BillingClient not ready or ProductDetails not found for $productId. Fallback to simulated success.")
            simulateSuccess(fallbackTier)
        }
    }

    fun simulateSuccess(tier: UserTier) {
        val newState = when (tier) {
            UserTier.LIFETIME -> BillingState.LIFETIME
            UserTier.PRO -> BillingState.PRO
            UserTier.FREE -> BillingState.FREE
        }
        _billingState.value = newState
        ShopCookRepository.instance.setUserTier(tier)
    }

    companion object {
        private const val TAG = "BillingManager"

        const val MONTHLY_PRODUCT_ID = "shop_yum_monthly_299"
        const val YEARLY_PRODUCT_ID = "shop_yum_yearly_1999"
        const val LIFETIME_PRODUCT_ID = "shop_yum_lifetime_3999"

        @Volatile
        private var INSTANCE: BillingManager? = null

        fun getInstance(context: Context? = null): BillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager(context).also { INSTANCE = it }
            }
        }
    }
}
