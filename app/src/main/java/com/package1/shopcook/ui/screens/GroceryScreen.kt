package com.package1.shopcook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.GroceryItem
import com.package1.shopcook.model.UserProfile
import com.package1.shopcook.ui.viewmodels.GroceryViewModel
import java.util.Locale

@Composable
fun GroceryScreen(viewModel: GroceryViewModel = viewModel()) {
    val groceryItems by viewModel.groceryItems.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    GroceryScreenContent(
        groceryItems = groceryItems,
        userProfile = userProfile,
        onToggleChecked = { viewModel.toggleChecked(it) },
        onAddItem = { name, category, amount, unit, price ->
            viewModel.addItem(name, category, amount, unit, price)
        },
        onClearChecked = { viewModel.clearChecked() },
        onRemoveItem = { viewModel.removeItem(it) },
        onStoreSelected = { viewModel.setSelectedStore(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryScreenContent(
    groceryItems: List<GroceryItem>,
    userProfile: UserProfile,
    onToggleChecked: (String) -> Unit = {},
    onAddItem: (String, GroceryCategory, Double, String, Double) -> Unit = { _, _, _, _, _ -> },
    onClearChecked: () -> Unit = {},
    onRemoveItem: (String) -> Unit = {},
    onStoreSelected: (String) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Active items are those not in kitchen. If they are in kitchen they are "Already have".
    // Or maybe they are all listed but kitchen ones are crossed out.
    // The total should exclude "Already have" items and "checked" items for the remaining budget.
    val activeItems = groceryItems.filter { !it.isAlreadyInKitchen && !it.isChecked }
    val totalEstimatedPrice = activeItems.sumOf { it.estimatedPrice }
    val checkedCount = groceryItems.count { it.isChecked }
    val kitchenSaved = groceryItems.filter { it.isAlreadyInKitchen }.sumOf { it.estimatedPrice }

    val groupedItems = remember(groceryItems) {
        groceryItems.groupBy { it.category }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shopping list",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${activeItems.size} items remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (checkedCount > 0) {
                            OutlinedButton(
                                onClick = onClearChecked,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Done ($checkedCount)")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Store Selector
                    var storeDropdownExpanded by remember { mutableStateOf(false) }
                    val stores = listOf("Shop Sunday", "Hannaford", "Trader Joe's", "Aldi", "Walmart", "Whole Foods")
                    
                    ExposedDropdownMenuBox(
                        expanded = storeDropdownExpanded,
                        onExpandedChange = { storeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = userProfile.selectedStore,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Store") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
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
                    Text(
                        text = "Prices are regionally adjusted based on store choice",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Summary Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Estimated Basket Total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", totalEstimatedPrice)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (kitchenSaved > 0) {
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", kitchenSaved)} off your list!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.padding(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Categorized Items
            groupedItems.forEach { (category, items) ->
                item {
                    Text(
                        text = category.displayName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(items, key = { it.id }) { item ->
                    GroceryItemRow(
                        item = item,
                        onToggleChecked = { onToggleChecked(item.id) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to add grocery item
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Grocery Item"
            )
        }
    }

    if (showAddDialog) {
        AddGroceryItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat, amount, unit, price ->
                onAddItem(name, cat, amount, unit, price)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GroceryItemRow(
    item: GroceryItem,
    onToggleChecked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStrikethrough = item.isChecked || item.isAlreadyInKitchen
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStrikethrough)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked || item.isAlreadyInKitchen,
                enabled = !item.isAlreadyInKitchen,
                onCheckedChange = { onToggleChecked() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isStrikethrough) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isStrikethrough) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (item.isAlreadyInKitchen) {
                    Text(
                        text = "Already have",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else if (item.recipeName != null) {
                    Text(
                        text = "For: ${item.recipeName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                text = "${item.amount.toInt()} ${item.unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "$${String.format(Locale.US, "%.2f", item.estimatedPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = if (isStrikethrough) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroceryItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, GroceryCategory, Double, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GroceryCategory.PRODUCE) }
    var amountText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("item") }
    var priceText by remember { mutableStateOf("2.50") }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Grocery Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        GroceryCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Qty") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Est. Price ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val amount = amountText.toDoubleOrNull() ?: 1.0
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, selectedCategory, amount, unit, price)
                    }
                }
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
