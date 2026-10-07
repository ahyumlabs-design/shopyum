package com.package1.shopcook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.KitchenItem
import com.package1.shopcook.model.Recipe
import androidx.lifecycle.viewmodel.compose.viewModel
import com.package1.shopcook.model.StorageLocation
import com.package1.shopcook.ui.viewmodels.KitchenViewModel
import java.util.Locale

@Composable
fun KitchenScreen(
    viewModel: KitchenViewModel = viewModel(),
    onRecipeClick: (String) -> Unit = {}
) {
    val kitchenItems by viewModel.kitchenItems.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val smartReminders = remember { viewModel.getSmartReminders() }

    KitchenScreenContent(
        kitchenItems = kitchenItems,
        recipes = recipes,
        smartReminders = smartReminders,
        onToggleAlreadyHave = { viewModel.toggleAlreadyHave(it) },
        onAddItem = { name, cat, loc, qty, unit, exp, leftover, servings ->
            viewModel.addItem(name, cat, loc, qty, unit, exp, leftover, servings)
        },
        onRecipeClick = onRecipeClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreenContent(
    kitchenItems: List<KitchenItem>,
    recipes: List<Recipe> = emptyList(),
    smartReminders: List<com.package1.shopcook.model.SmartReminder> = emptyList(),
    onToggleAlreadyHave: (String) -> Unit = {},
    onAddItem: (String, GroceryCategory, StorageLocation, Double, String, Int?, Boolean, Int?) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onRecipeClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showWhatCanIMakeDialog by remember { mutableStateOf(false) }

    val savedAmount = kitchenItems.filter { it.isAlreadyHave }.sumOf { it.estimatedValue }
    val savedCount = kitchenItems.count { it.isAlreadyHave }

    val leftovers = kitchenItems.filter { it.isLeftover }
    val staples = kitchenItems.filter { !it.isLeftover && (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)) }
    val groupedStaples = staples.groupBy { it.category }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", savedAmount)} off your list - $savedCount items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tick everything you already have. It comes straight off your grocery bill — and stays off.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Smart Reminders
            if (smartReminders.isNotEmpty()) {
                item {
                    SmartRemindersSection(
                        reminders = smartReminders,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }

            // Feature 2: Reverse Recipe Search ("What Can I Make?" / SuperCook Style) Button Card
            item {
                Card(
                    onClick = { showWhatCanIMakeDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "What Can I Make With My Pantry?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "SuperCook style recipe matcher",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (leftovers.isNotEmpty()) {
                item {
                    Text("LEFTOVERS YOU FROZE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(leftovers, key = { it.id }) { item ->
                    KitchenItemRow(item = item, onToggle = { onToggleAlreadyHave(item.id) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search staples") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            groupedStaples.forEach { (category, items) ->
                item {
                    Text(
                        text = category.displayName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(items, key = { it.id }) { item ->
                    KitchenItemRow(item = item, onToggle = { onToggleAlreadyHave(item.id) })
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Staple")
        }
    }

    if (showAddDialog) {
        AddStapleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat ->
                onAddItem(name, cat, StorageLocation.PANTRY, 1.0, "item", null, false, null)
                showAddDialog = false
            }
        )
    }

    if (showWhatCanIMakeDialog) {
        WhatCanIMakeDialog(
            kitchenItems = kitchenItems,
            recipes = recipes,
            onDismiss = { showWhatCanIMakeDialog = false },
            onRecipeClick = onRecipeClick
        )
    }
}

@Composable
fun WhatCanIMakeDialog(
    kitchenItems: List<KitchenItem>,
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val pantryNames = remember(kitchenItems) {
        kitchenItems.filter { it.isAlreadyHave }.map { it.name.lowercase() }.toSet()
    }

    val matchingRecipes = remember(recipes, pantryNames) {
        recipes.map { recipe ->
            val totalIngs = recipe.ingredients.size
            val matchedCount = recipe.ingredients.count { ing ->
                pantryNames.any { pantry -> ing.name.lowercase().contains(pantry) || pantry.contains(ing.name.lowercase()) }
            }
            val matchPercentage = if (totalIngs > 0) matchedCount.toDouble() / totalIngs.toDouble() else 0.0
            Triple(recipe, matchedCount, matchPercentage)
        }.sortedByDescending { it.third }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🍳 What Can I Make Now?", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(matchingRecipes, key = { it.first.id }) { (recipe, matchedCount, percentage) ->
                    val isReady = percentage >= 0.4 || matchedCount >= 2
                    Card(
                        onClick = {
                            onDismiss()
                            onRecipeClick(recipe.id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isReady) "🔥 Ready to cook! ($matchedCount/${recipe.ingredients.size} ingredients matched)"
                                           else "Missing items ($matchedCount/${recipe.ingredients.size} matched)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun KitchenItemRow(
    item: KitchenItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle) {
            if (item.isAlreadyHave) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Have it", tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Outlined.Circle, contentDescription = "Don't have", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column {
            Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (item.isLeftover && item.expiryDaysRemaining != null) {
                Text("Expires in ${item.expiryDaysRemaining} days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStapleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, GroceryCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GroceryCategory.PANTRY) }
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pantry Staple") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Staple Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        GroceryCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onConfirm(name, selectedCategory)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
