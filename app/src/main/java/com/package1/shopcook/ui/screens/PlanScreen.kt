package com.package1.shopcook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.package1.shopcook.ads.AdsManager
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.ui.viewmodels.PlanViewModel
import com.package1.shopcook.util.DateUtils

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun PlanScreen(viewModel: PlanViewModel = viewModel()) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    PlanScreenContent(
        userProfile = userProfile,
        onWeekSelected = { viewModel.selectWeek(it) },
        onServingsChange = { viewModel.setServingsCount(it) },
        onDaysChange = { viewModel.setActiveCookingDays(it) },
        onBudgetChange = { viewModel.setGroceryBudget(it) },
        onCheapestWeekChange = { viewModel.setIsCheapestWeek(it) },
        onDietaryFilterToggle = { viewModel.toggleDietaryFilter(it) },
        onStoreSelected = { viewModel.setSelectedStore(it) },
        onZipCodeChange = { viewModel.setZipCode(it) },
        onAddExcludedIngredient = { viewModel.addExcludedIngredient(it) },
        onRemoveExcludedIngredient = { viewModel.removeExcludedIngredient(it) },
        onGeneratePlan = {
            AdsManager.showInterstitial(context, userProfile.userTier) {
                viewModel.generateCostOptimizedPlan()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlanScreenContent(
    userProfile: UserProfile,
    onWeekSelected: (String) -> Unit = {},
    onServingsChange: (Int) -> Unit = {},
    onDaysChange: (List<String>) -> Unit = {},
    onBudgetChange: (Double) -> Unit = {},
    onCheapestWeekChange: (Boolean) -> Unit = {},
    onDietaryFilterToggle: (String) -> Unit = {},
    onStoreSelected: (String) -> Unit = {},
    onZipCodeChange: (String) -> Unit = {},
    onAddExcludedIngredient: (String) -> Unit = {},
    onRemoveExcludedIngredient: (String) -> Unit = {},
    onGeneratePlan: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Week Picker Header
        item {
            Text(
                text = "Week of ${userProfile.selectedWeek}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            val weekOptions = remember { DateUtils.getWeekOptions() }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weekOptions) { week ->
                    FilterChip(
                        selected = userProfile.selectedWeek == week,
                        onClick = { onWeekSelected(week) },
                        label = {
                            Text(
                                text = week,
                                fontWeight = if (userProfile.selectedWeek == week) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary & Generate Button
        item {
            val planCount = if (userProfile.isProUser) 5 else 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$planCount dinners planned${if (!userProfile.isProUser) " (Free tier)" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!userProfile.isProUser) {
                    Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                        Text("Upgrade to Pro for 7 dinners", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGeneratePlan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (userProfile.isCheapestWeek) "Build the cheapest week" else "Build a different week")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // COOKING FOR section
        item {
            Text("COOKING FOR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onServingsChange((userProfile.servingsCount - 1).coerceAtLeast(1)) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease servings")
                }
                Text("${userProfile.servingsCount} people", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { onServingsChange((userProfile.servingsCount + 1).coerceAtMost(10)) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase servings")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // NIGHTS YOU'RE COOKING section
        item {
            Text("NIGHTS YOU'RE COOKING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            val distinctDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            val displayDays = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                distinctDays.forEachIndexed { index, day ->
                    val isSelected = userProfile.activeCookingDays.contains(day)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newDays = if (isSelected) {
                                userProfile.activeCookingDays - day
                            } else {
                                userProfile.activeCookingDays + day
                            }
                            onDaysChange(newDays)
                        },
                        label = { Text(displayDays[index]) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // GROCERY BUDGET section
        item {
            Text("GROCERY BUDGET", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$${userProfile.groceryBudget.toInt()}", style = MaterialTheme.typography.titleLarge)
            Slider(
                value = userProfile.groceryBudget.toFloat(),
                onValueChange = { onBudgetChange(it.toDouble()) },
                valueRange = 20f..300f,
                steps = 28
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Just build the cheapest week toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCheapestWeekChange(!userProfile.isCheapestWeek) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Just build the cheapest week", style = MaterialTheme.typography.titleMedium)
                Switch(checked = userProfile.isCheapestWeek, onCheckedChange = onCheapestWeekChange)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dietary filters selector
        item {
            Text("DIETARY FILTERS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            val filters = listOf("Keto", "Paleo", "Vegetarian", "Gluten-Free", "Low Carb")

            Column {
                filters.chunked(3).forEach { rowFilters ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowFilters.forEach { filter ->
                            val isSelected = userProfile.selectedDietaryFilters.contains(filter)
                            FilterChip(
                                selected = isSelected,
                                onClick = { if (userProfile.isProUser) onDietaryFilterToggle(filter) },
                                label = { Text(filter) },
                                leadingIcon = {
                                    if (!userProfile.isProUser) {
                                        Icon(Icons.Default.Lock, contentDescription = "Pro Feature", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // STORE MATCHING WITH ZIP CODE
        item {
            Text("PREFERRED STORE & ZIP CODE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            var storeDropdownExpanded by remember { mutableStateOf(false) }
            val stores = listOf("Aldi", "Trader Joe's", "Walmart", "Hannaford", "Whole Foods", "Shop Sunday")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Store Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = storeDropdownExpanded,
                    onExpandedChange = { storeDropdownExpanded = it },
                    modifier = Modifier.weight(1.3f)
                ) {
                    OutlinedTextField(
                        value = userProfile.selectedStore,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Store Chain") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = storeDropdownExpanded,
                        onDismissRequest = { storeDropdownExpanded = false }
                    ) {
                        stores.forEach { store ->
                            DropdownMenuItem(
                                text = { Text(store) },
                                onClick = {
                                    onStoreSelected(store)
                                    storeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // ZIP Code Field
                OutlinedTextField(
                    value = userProfile.zipCode,
                    onValueChange = { onZipCodeChange(it) },
                    label = { Text("ZIP Code") },
                    placeholder = { Text("90210") },
                    singleLine = true,
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // INGREDIENT EXCLUSIONS BY NAME
        item {
            Text("EXCLUDED INGREDIENTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Type ingredients to omit from recipes & grocery lists (e.g. corn, cumin, peanuts, cilantro)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            var newExcludedText by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newExcludedText,
                    onValueChange = { newExcludedText = it },
                    placeholder = { Text("e.g. peanuts, cilantro") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (newExcludedText.isNotBlank()) {
                            onAddExcludedIngredient(newExcludedText)
                            newExcludedText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Exclude")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Exclusions Chips
            if (userProfile.excludedIngredients.isNotEmpty()) {
                Text("Currently Excluded:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userProfile.excludedIngredients.forEach { excludedItem ->
                        InputChip(
                            selected = true,
                            onClick = { onRemoveExcludedIngredient(excludedItem) },
                            label = { Text(excludedItem) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $excludedItem",
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

            // Preset Quick Add Suggestion Chips
            Spacer(modifier = Modifier.height(8.dp))
            Text("Quick Exclude Suggestions:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            val quickSuggestions = listOf("corn", "cumin", "peanuts", "cilantro", "mushrooms", "dairy")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSuggestions.forEach { suggestion ->
                    val isAlreadyExcluded = userProfile.excludedIngredients.any { it.equals(suggestion, ignoreCase = true) }
                    FilterChip(
                        selected = isAlreadyExcluded,
                        onClick = {
                            if (isAlreadyExcluded) {
                                onRemoveExcludedIngredient(suggestion)
                            } else {
                                onAddExcludedIngredient(suggestion)
                            }
                        },
                        label = { Text("+ $suggestion") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
