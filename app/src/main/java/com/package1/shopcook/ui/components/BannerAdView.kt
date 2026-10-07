package com.package1.shopcook.ui.components

import android.view.Gravity
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.package1.shopcook.ads.AdsManager
import com.package1.shopcook.model.BillingState
import com.package1.shopcook.model.UserTier
import com.package1.shopcook.ui.theme.ShopCookTheme

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    isAdFree: Boolean = AdsManager.isAdFree(),
    adUnitId: String = AdsManager.TEST_BANNER_AD_UNIT_ID
) {
    if (isAdFree) {
        // Automatically hide itself (zero height) if user is PRO or LIFETIME
        Spacer(modifier = modifier.height(0.dp))
        return
    }

    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        BannerAdCardFallback(modifier = modifier)
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                try {
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        setAdUnitId(adUnitId)
                        loadAd(AdRequest.Builder().build())
                    }
                } catch (_: Throwable) {
                    TextView(context).apply {
                        text = "Advertisement"
                        gravity = Gravity.CENTER
                    }
                }
            },
            update = { view ->
                if (view is AdView) {
                    try {
                        view.loadAd(AdRequest.Builder().build())
                    } catch (_: Throwable) {}
                }
            }
        )
    }
}

@Composable
private fun BannerAdCardFallback(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "ADVERTISEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Sponsored",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fresh Organic Groceries Delivered",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Save up to 20% on weekly meal prep ingredients today!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BannerAdViewFromBillingState(
    modifier: Modifier = Modifier,
    billingState: BillingState,
    adUnitId: String = AdsManager.TEST_BANNER_AD_UNIT_ID
) {
    BannerAdView(
        modifier = modifier,
        isAdFree = billingState.isAdFree,
        adUnitId = adUnitId
    )
}

@Composable
fun BannerAdViewFromTier(
    modifier: Modifier = Modifier,
    userTier: UserTier,
    adUnitId: String = AdsManager.TEST_BANNER_AD_UNIT_ID
) {
    BannerAdView(
        modifier = modifier,
        isAdFree = userTier.isAdFree,
        adUnitId = adUnitId
    )
}

@Preview(showBackground = true)
@Composable
fun BannerAdViewFreePreview() {
    ShopCookTheme {
        BannerAdView(isAdFree = false)
    }
}

@Preview(showBackground = true)
@Composable
fun BannerAdViewProPreview() {
    ShopCookTheme {
        BannerAdView(isAdFree = true)
    }
}
