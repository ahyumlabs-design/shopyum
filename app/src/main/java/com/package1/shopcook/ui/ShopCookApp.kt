package com.package1.shopcook.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.ui.text.style.TextAlign
import androidx.window.core.layout.WindowSizeClass
import com.package1.shopcook.navigation.TopLevelDestination
import com.package1.shopcook.ui.screens.GroceryScreen
import com.package1.shopcook.ui.screens.HomeScreen
import com.package1.shopcook.ui.screens.KitchenScreen
import com.package1.shopcook.ui.screens.PlanScreen
import com.package1.shopcook.ui.screens.RecipeDetailScreen
import com.package1.shopcook.ui.screens.YouScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShopCookApp() {
    var currentTabRoute by rememberSaveable { mutableStateOf("home") }
    var selectedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }

    // Intercept system back press when recipe detail is open
    BackHandler(enabled = selectedRecipeId != null) {
        selectedRecipeId = null
    }

    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass
    val isLargeScreen = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (isLargeScreen) {
        // Large screen layout: NavigationRail on left, content area on right
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail {
                Spacer(modifier = Modifier.height(12.dp))
                TopLevelDestination.entries.forEach { destination ->
                    val route = destination.name.lowercase()
                    val isSelected = currentTabRoute == route
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            Log.d("ShopCook", "Rail tab clicked: ${route}")
                            selectedRecipeId = null
                            currentTabRoute = route
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ShopCookContent(
                    currentTabRoute = currentTabRoute,
                    selectedRecipeId = selectedRecipeId,
                    isLargeScreen = true,
                    onRecipeClick = { id -> selectedRecipeId = id },
                    onBackClick = { selectedRecipeId = null }
                )
            }
        }
    } else {
        // Compact screen layout (phones): Bottom navigation bar
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val route = destination.name.lowercase()
                        val isSelected = selectedRecipeId == null && currentTabRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                Log.d("ShopCook", "Bottom tab clicked: ${route}")
                                selectedRecipeId = null
                                currentTabRoute = route
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ShopCookContent(
                    currentTabRoute = currentTabRoute,
                    selectedRecipeId = selectedRecipeId,
                    isLargeScreen = false,
                    onRecipeClick = { id -> selectedRecipeId = id },
                    onBackClick = { selectedRecipeId = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ShopCookContent(
    currentTabRoute: String,
    selectedRecipeId: String?,
    isLargeScreen: Boolean,
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    if (isLargeScreen && (currentTabRoute == "home" || currentTabRoute == "kitchen")) {
        val navigator = rememberListDetailPaneScaffoldNavigator<String>()
        val scope = rememberCoroutineScope()

        LaunchedEffect(selectedRecipeId) {
            if (selectedRecipeId != null && navigator.currentDestination?.contentKey != selectedRecipeId) {
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, selectedRecipeId)
            } else if (selectedRecipeId == null && navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
                navigator.navigateBack()
            }
        }

        NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    TabScreenContent(
                        currentTabRoute = currentTabRoute,
                        onRecipeClick = { id ->
                            onRecipeClick(id)
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                            }
                        }
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val recipeId = navigator.currentDestination?.contentKey ?: selectedRecipeId
                    if (recipeId != null) {
                        RecipeDetailScreen(
                            recipeId = recipeId,
                            onBackClick = {
                                onBackClick()
                                scope.launch { navigator.navigateBack() }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Select a Recipe",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Choose a recipe from your meal plan or explore list to view full cooking instructions and ingredients side-by-side.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        )
    } else {
        if (selectedRecipeId != null) {
            RecipeDetailScreen(
                recipeId = selectedRecipeId,
                onBackClick = onBackClick
            )
        } else {
            TabScreenContent(currentTabRoute = currentTabRoute, onRecipeClick = onRecipeClick)
        }
    }
}

@Composable
fun TabScreenContent(
    currentTabRoute: String,
    onRecipeClick: (String) -> Unit
) {
    when (currentTabRoute) {
        "home" -> HomeScreen(onRecipeClick = onRecipeClick)
        "plan" -> PlanScreen()
        "grocery" -> GroceryScreen()
        "kitchen" -> KitchenScreen(onRecipeClick = onRecipeClick)
        "you" -> YouScreen()
        else -> HomeScreen(onRecipeClick = onRecipeClick)
    }
}
