package com.package1.shopcook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.package1.shopcook.R
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.package1.shopcook.data.MockData
import com.package1.shopcook.model.GroceryCategory
import com.package1.shopcook.model.Recipe
import com.package1.shopcook.model.RecipeIngredient
import com.package1.shopcook.model.UserTier
import com.package1.shopcook.ui.components.BannerAdView
import com.package1.shopcook.ui.theme.ShopCookTheme
import com.package1.shopcook.ui.viewmodels.HomeViewModel
import com.package1.shopcook.ui.viewmodels.YouViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBackClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    youViewModel: YouViewModel = viewModel()
) {
    val recipes by homeViewModel.recipes.collectAsState()
    val userProfile by youViewModel.userProfile.collectAsState()
    val recipe = homeViewModel.getRecipeById(recipeId)
        ?: recipes.find { it.id == recipeId }
        ?: MockData.sampleRecipes.find { it.id == recipeId }
        ?: MockData.sampleRecipes.first()

    RecipeDetailScreen(
        recipe = recipe,
        userTier = userProfile.userTier,
        isAdFree = userProfile.isAdFree,
        onBackClick = onBackClick,
        onFavoriteToggle = { homeViewModel.toggleFavorite(recipe.id) },
        onAddGrocery = { homeViewModel.addRecipeToGrocery(recipe) },
        onCookedThis = {
            val mealItem = homeViewModel.mealPlan.value.find { it.recipe.id == recipe.id }
            if (mealItem != null) {
                homeViewModel.toggleMealCooked(mealItem.id)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    userTier: UserTier = UserTier.FREE,
    isAdFree: Boolean = userTier.isAdFree,
    onBackClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {},
    onAddGrocery: () -> Unit = {},
    onCookedThis: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isCookedState by remember { mutableStateOf(false) }
    var showCookMode by remember { mutableStateOf(false) }
    var currentServings by remember { mutableStateOf(recipe.servings.coerceAtLeast(1)) }
    val scalingFactor = currentServings.toDouble() / recipe.servings.toDouble()
    val scaledIngredients = remember(recipe.ingredients, scalingFactor) {
        recipe.ingredients.map { it.copy(amount = it.amount * scalingFactor) }
    }

    if (showCookMode) {
        CookModeDialog(
            recipe = recipe,
            onDismiss = { showCookMode = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (recipe.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cooked This Action
                    OutlinedButton(
                        onClick = {
                            isCookedState = !isCookedState
                            onCookedThis()
                            scope.launch {
                                val message = if (isCookedState) "Marked as cooked! Great job Chef 👨‍🍳" else "Unmarked as cooked"
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCookedState) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isCookedState) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCookedState) "Cooked!" else "Cooked This",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // Add to Grocery List Action
                    Button(
                        onClick = {
                            onAddGrocery()
                            scope.launch {
                                snackbarHostState.showSnackbar("Added ${recipe.ingredients.count { !it.isPantryStaple }} ingredients to Grocery List! 🛒")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add to Grocery",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Top Image / Dish Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        error = painterResource(R.drawable.ic_launcher_foreground)
                    )
                }
            }

            // Recipe Detail Content Sheet Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badges Row: MAKE IN 35m | 2 serv. | $2.69/serv
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // MAKE IN Badges
                        AssistChip(
                            onClick = {},
                            label = { Text("MAKE IN ${recipe.cookTimeMinutes + recipe.prepTimeMinutes}m", fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Servings Badge
                        AssistChip(
                            onClick = {},
                            label = { Text("${recipe.servings} serv.", fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Restaurant,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.secondary
                            )
                        )

                        // Price per serving
                        AssistChip(
                            onClick = {},
                            label = { Text("$${String.format(Locale.US, "%.2f", recipe.pricePerServing)}/serv", fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description text
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Effort Metrics Card
                    Text(
                        text = "EFFORT BREAKDOWN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EffortMetricItem(
                                icon = Icons.Default.FitnessCenter,
                                label = "DIFFICULTY",
                                value = recipe.difficulty.displayName
                            )

                            Surface(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            ) {}

                            EffortMetricItem(
                                icon = Icons.Default.ShoppingBag,
                                label = "ITEMS",
                                value = "${recipe.ingredients.size} count"
                            )

                            Surface(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            ) {}

                            EffortMetricItem(
                                icon = Icons.Default.CleaningServices,
                                label = "CLEANUP",
                                value = recipe.cleanupLevel.label
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Nutritional Breakdown Card (Feature 2)
                    Text(
                        text = "NUTRITIONAL BREAKDOWN (Per Serving)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EffortMetricItem(
                                icon = Icons.Outlined.LocalDining,
                                label = "CALORIES",
                                value = "${recipe.calories ?: 500} kcal"
                            )
                            Surface(modifier = Modifier.width(1.dp).height(40.dp), color = MaterialTheme.colorScheme.outlineVariant) {}
                            EffortMetricItem(
                                icon = Icons.Default.FitnessCenter,
                                label = "PROTEIN",
                                value = "${recipe.proteinGrams ?: 35}g"
                            )
                            Surface(modifier = Modifier.width(1.dp).height(40.dp), color = MaterialTheme.colorScheme.outlineVariant) {}
                            EffortMetricItem(
                                icon = Icons.Outlined.Grain,
                                label = "CARBS",
                                value = "${recipe.carbsGrams ?: 40}g"
                            )
                            Surface(modifier = Modifier.width(1.dp).height(40.dp), color = MaterialTheme.colorScheme.outlineVariant) {}
                            EffortMetricItem(
                                icon = Icons.Outlined.Egg,
                                label = "FAT",
                                value = "${recipe.fatGrams ?: 15}g"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Feature 1: Dynamic Serving Scaler Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                                    text = "SERVINGS SCALER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$currentServings Servings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (currentServings > 1) currentServings-- },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .size(36.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                }

                                Text(
                                    text = "$currentServings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(24.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                IconButton(
                                    onClick = { if (currentServings < 20) currentServings++ },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .size(36.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // INGREDIENTS Section Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INGREDIENTS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )

                        Text(
                            text = "${scaledIngredients.size} total items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visual Grid of Ingredients (2 Columns)
                    IngredientVisualCardsGrid(ingredients = scaledIngredients)

                    Spacer(modifier = Modifier.height(24.dp))

                    // INSTRUCTIONS Section with Start Cook Mode Button (Feature 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STEP-BY-STEP INSTRUCTIONS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )

                        Button(
                            onClick = { showCookMode = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Cook Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Step-by-Step Instructions
            itemsIndexed(recipe.instructions) { index, step ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                BannerAdView(isAdFree = userTier.isAdFree)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// Feature 1: Cook Mode Dialog & Immersive View with Timers
data class ParsedTimer(val durationSeconds: Int, val label: String)

fun extractTimersFromStep(stepText: String): List<ParsedTimer> {
    val regex = Regex("(\\d+(?:-\\d+)?)\\s*(mins?|minutes?|hrs?|hours?)", RegexOption.IGNORE_CASE)
    val matches = regex.findAll(stepText)
    val timers = mutableListOf<ParsedTimer>()
    for (match in matches) {
        val numStr = match.groupValues[1]
        val unitStr = match.groupValues[2].lowercase()
        val baseNum = if (numStr.contains("-")) {
            numStr.split("-").first().toIntOrNull() ?: 5
        } else {
            numStr.toIntOrNull() ?: 5
        }
        val seconds = if (unitStr.startsWith("hr") || unitStr.startsWith("hour")) {
            baseNum * 3600
        } else {
            baseNum * 60
        }
        timers.add(ParsedTimer(seconds, match.value))
    }
    return timers
}

@Composable
fun CookModeDialog(
    recipe: Recipe,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val instructions = recipe.instructions

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Step ${currentStep + 1} of ${instructions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Exit Cook Mode"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / instructions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Immersive Step Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${currentStep + 1}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val stepText = instructions.getOrElse(currentStep) { "" }
                        Text(
                            text = stepText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Interactive Timers for this step
                        val parsedTimers = remember(stepText) { extractTimersFromStep(stepText) }
                        if (parsedTimers.isNotEmpty()) {
                            Text(
                                text = "Embedded Cooking Timers",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            parsedTimers.forEach { timer ->
                                InteractiveCountdownTimerWidget(
                                    initialSeconds = timer.durationSeconds,
                                    timerLabel = timer.label
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Navigation (Previous / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Previous Step", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (currentStep < instructions.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (currentStep < instructions.size - 1) "Next Step" else "Finish Cooking 👨‍🍳",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveCountdownTimerWidget(
    initialSeconds: Int,
    timerLabel: String
) {
    var remainingSeconds by remember(initialSeconds) { mutableStateOf(initialSeconds) }
    var isRunning by remember(initialSeconds) { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            if (remainingSeconds == 0) {
                isRunning = false
            }
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Timer ($timerLabel)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (remainingSeconds == 0) "Time's up! 🎉" else timeFormatted,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                IconButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = initialSeconds
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EffortMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun IngredientVisualCardsGrid(ingredients: List<RecipeIngredient>) {
    val chunked = remember(ingredients) { ingredients.chunked(2) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunked.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { ingredient ->
                    IngredientVisualCard(
                        ingredient = ingredient,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun IngredientVisualCard(
    ingredient: RecipeIngredient,
    modifier: Modifier = Modifier
) {
    val (icon, tileColor, iconTint) = remember(ingredient) {
        getIngredientVisualInfo(ingredient)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tileColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = ingredient.name,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val displayAmount = if (ingredient.amount == ingredient.amount.toInt().toDouble()) {
                    ingredient.amount.toInt().toString()
                } else {
                    String.format(Locale.US, "%.1f", ingredient.amount)
                }
                Text(
                    text = "$displayAmount ${ingredient.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (ingredient.isPantryStaple) {
                    Text(
                        text = "Pantry Staple",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun getIngredientVisualInfo(ingredient: RecipeIngredient): Triple<ImageVector, Color, Color> {
    return when (ingredient.category) {
        GroceryCategory.MEAT -> Triple(
            Icons.Outlined.SetMeal,
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        )
        GroceryCategory.PRODUCE -> Triple(
            Icons.Outlined.Grass,
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        )
        GroceryCategory.DAIRY -> Triple(
            Icons.Outlined.Egg,
            Color(0xFFFFF8E1),
            Color(0xFFF57F17)
        )
        GroceryCategory.PANTRY -> Triple(
            Icons.Outlined.Grain,
            Color(0xFFEFEBE9),
            Color(0xFF4E342E)
        )
        GroceryCategory.FROZEN -> Triple(
            Icons.Outlined.WaterDrop,
            Color(0xFFE0F7FA),
            Color(0xFF00838F)
        )
        else -> Triple(
            Icons.Outlined.LocalDining,
            Color(0xFFF3E5F5),
            Color(0xFF6A1B9A)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    ShopCookTheme {
        RecipeDetailScreen(recipe = MockData.sampleRecipes.first())
    }
}
