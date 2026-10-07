package com.package1.shopcook.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.package1.shopcook.data.MockData
import com.package1.shopcook.data.SyncStatus
import androidx.lifecycle.viewmodel.compose.viewModel
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.model.UserTier
import com.package1.shopcook.ui.theme.ShopCookTheme
import com.package1.shopcook.ui.viewmodels.YouViewModel

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedButton

@Composable
fun YouScreen(
    viewModel: YouViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isCloudSyncEnabled by viewModel.isCloudSyncEnabled.collectAsState()
    val pendingQueue by viewModel.pendingQueue.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    YouScreenContent(
        profile = profile,
        syncStatus = syncStatus,
        isCloudSyncEnabled = isCloudSyncEnabled,
        pendingQueueSize = pendingQueue.size,
        lastSyncTime = lastSyncTime,
        onSetUserTier = { viewModel.setUserTier(it) },
        onSetServings = { viewModel.setServingsCount(it) },
        onToggleCookingDay = { day ->
            val currentDays = profile.activeCookingDays.toMutableList()
            if (currentDays.contains(day)) currentDays.remove(day) else currentDays.add(day)
            viewModel.setActiveCookingDays(currentDays)
        },
        onToggleDietaryFilter = { viewModel.toggleDietaryFilter(it) },
        onSetStore = { viewModel.setSelectedStore(it) },
        onZipCodeChange = { viewModel.setZipCode(it) },
        onAddExcludedIngredient = { viewModel.addExcludedIngredient(it) },
        onRemoveExcludedIngredient = { viewModel.removeExcludedIngredient(it) },
        onJoinHousehold = { viewModel.joinHousehold(it) },
        onLeaveHousehold = { viewModel.leaveHousehold() },
        onGenerateNewShareCode = { viewModel.generateNewShareCode() },
        onToggleCloudSync = { viewModel.toggleCloudSync(it) },
        onForceSync = { viewModel.forceSync() },
        onBuyMonthly = { viewModel.buyMonthly(activity) },
        onBuyYearly = { viewModel.buyYearly(activity) },
        onBuyLifetime = { viewModel.buyLifetime(activity) }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun YouScreenContent(
    profile: UserProfile,
    syncStatus: SyncStatus = SyncStatus.IDLE,
    isCloudSyncEnabled: Boolean = true,
    pendingQueueSize: Int = 0,
    lastSyncTime: Long = 0L,
    onSetUserTier: (UserTier) -> Unit = {},
    onSetServings: (Int) -> Unit = {},
    onToggleCookingDay: (String) -> Unit = {},
    onToggleDietaryFilter: (String) -> Unit = {},
    onSetStore: (String) -> Unit = {},
    onZipCodeChange: (String) -> Unit = {},
    onAddExcludedIngredient: (String) -> Unit = {},
    onRemoveExcludedIngredient: (String) -> Unit = {},
    onJoinHousehold: (String) -> Unit = {},
    onLeaveHousehold: () -> Unit = {},
    onGenerateNewShareCode: () -> Unit = {},
    onToggleCloudSync: (Boolean) -> Unit = {},
    onForceSync: () -> Unit = {},
    onBuyMonthly: () -> Unit = {},
    onBuyYearly: () -> Unit = {},
    onBuyLifetime: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // User Profile Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(90.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Developer Menu for Billing Status / UserTier
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Developer Menu", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Select Active Billing Status / UserTier", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserTier.entries.forEach { tier ->
                            FilterChip(
                                selected = profile.userTier == tier,
                                onClick = { onSetUserTier(tier) },
                                label = { Text(tier.name) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Subscription Banner & Options
        item {
            if (profile.isAdFree) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WorkspacePremium,
                            contentDescription = "Ad-Free Member",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You are on ${profile.userTier.name} Tier!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Enjoy unlimited planning, dietary filters, and zero ads.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Upgrade to Pro or Lifetime",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock 5-7 dinners/week, dietary filters, store selection & zero ads!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Subscription Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubscriptionOptionCard(
                                title = "Monthly",
                                price = "$2.99",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onBuyMonthly() }
                            )
                            SubscriptionOptionCard(
                                title = "Yearly",
                                price = "$19.99",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onBuyYearly() }
                            )
                            SubscriptionOptionCard(
                                title = "Lifetime",
                                price = "$39.99",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onBuyLifetime() }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Features Comparison (Free vs Pro)
        item {
            Text(
                text = "Features Comparison",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureRow("Weekly Meal Plan", "3 Dinners", "5-7 Dinners")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureRow("Ads", "Yes", "No")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureRow("Store Selection", "No", "Yes")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureRow("Dietary Filters", "No", "Yes")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureRow("Grocery List Aisle Sorting", "No", "Yes")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FeatureRow("Ingredient Tracking", "No", "Yes")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Household Sharing via Share Code Card
        item {
            Text(
                text = "Household Sharing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.People, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Household Meal & Grocery Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Sync meal plans & grocery lists live with family or partner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unique Share Code Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Your Share Code", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(profile.shareCode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                            OutlinedButton(
                                onClick = { onGenerateNewShareCode() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("New Code")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Join Household Section
                    if (profile.joinedShareCode == null) {
                        var partnerCodeInput by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = partnerCodeInput,
                                onValueChange = { partnerCodeInput = it.uppercase() },
                                label = { Text("Partner Share Code") },
                                placeholder = { Text("e.g. YUM-8924") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    if (partnerCodeInput.isNotBlank()) {
                                        onJoinHousehold(partnerCodeInput)
                                        partnerCodeInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Join")
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Connected Household", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text(profile.joinedShareCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                TextButton(onClick = { onLeaveHousehold() }) {
                                    Text("Leave", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Settings section
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Servings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.FamilyRestroom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Default Servings", fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (profile.servingsCount > 1) onSetServings(profile.servingsCount - 1) }) {
                                Text("-", style = MaterialTheme.typography.titleLarge)
                            }
                            Text("${profile.servingsCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { if (profile.servingsCount < 10) onSetServings(profile.servingsCount + 1) }) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Cooking Days
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Default Cooking Days", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        days.forEach { day ->
                            FilterChip(
                                selected = profile.activeCookingDays.contains(day),
                                onClick = { onToggleCookingDay(day) },
                                label = { Text(day.substring(0, 3)) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Dietary Preferences
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dietary Preferences", fontWeight = FontWeight.SemiBold)
                    }
                    if (!profile.isProUser) {
                        Text("Upgrade to Pro to use dietary filters.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val diets = listOf("Vegetarian", "Vegan", "Gluten Free", "High Protein", "Low Carb", "Keto", "Dairy Free")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        diets.forEach { diet ->
                            FilterChip(
                                selected = profile.selectedDietaryFilters.contains(diet),
                                onClick = { if (profile.isProUser) onToggleDietaryFilter(diet) },
                                label = { Text(diet) },
                                enabled = profile.isProUser
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Default Store & ZIP Code
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Default Store & ZIP Code", fontWeight = FontWeight.SemiBold)
                    }
                    if (!profile.isProUser) {
                        Text("Upgrade to Pro to select preferred store.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val stores = listOf("Aldi", "Trader Joe's", "Walmart", "Hannaford", "Whole Foods", "Shop Sunday")
                    var expanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { if (profile.isProUser) expanded = !expanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = profile.selectedStore,
                                onValueChange = {},
                                readOnly = true,
                                enabled = profile.isProUser,
                                label = { Text("Store Chain") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                stores.forEach { store ->
                                    DropdownMenuItem(
                                        text = { Text(store) },
                                        onClick = {
                                            onSetStore(store)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = profile.zipCode,
                            onValueChange = { onZipCodeChange(it) },
                            label = { Text("ZIP Code") },
                            placeholder = { Text("90210") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Excluded Ingredients Section
                    Text("Excluded Ingredients", fontWeight = FontWeight.SemiBold)
                    Text("Type ingredients to exclude from recipes & grocery lists", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    var newExclusionText by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newExclusionText,
                            onValueChange = { newExclusionText = it },
                            placeholder = { Text("e.g. corn, cumin") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (newExclusionText.isNotBlank()) {
                                    onAddExcludedIngredient(newExclusionText)
                                    newExclusionText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Exclude")
                        }
                    }

                    if (profile.excludedIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile.excludedIngredients.forEach { item ->
                                InputChip(
                                    selected = true,
                                    onClick = { onRemoveExcludedIngredient(item) },
                                    label = { Text(item) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove $item",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                                        selectedTrailingIconColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Cloud Sync & Backup Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Cloud Sync (Local-First)", fontWeight = FontWeight.SemiBold)
                                val statusText = when (val s = syncStatus) {
                                    is SyncStatus.IDLE -> "Status: Idle"
                                    is SyncStatus.SYNCING -> "Status: Syncing..."
                                    is SyncStatus.SUCCESS -> "Status: Synced Successfully"
                                    is SyncStatus.ERROR -> "Status: Error (${s.message})"
                                }
                                Text(statusText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (pendingQueueSize > 0) {
                                    Text("Pending changes: $pendingQueueSize", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                        Switch(
                            checked = isCloudSyncEnabled,
                            onCheckedChange = onToggleCloudSync
                        )
                    }
                    if (isCloudSyncEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onForceSync) {
                                Text("Sync Now")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SubscriptionOptionCard(title: String, price: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun FeatureRow(feature: String, freeText: String, proText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = freeText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text = proText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun YouScreenPreview() {
    ShopCookTheme {
        YouScreenContent(profile = MockData.sampleUserProfile)
    }
}
