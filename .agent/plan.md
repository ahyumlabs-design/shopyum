# Project Plan

Build an Android app similar to TapCook: Budget Meal Plans. The app helps users plan weekly budget meals, customize servings and cooking nights, track grocery lists with estimated prices, manage kitchen/pantry inventory to avoid double purchases, and view recipe details with cooking effort (prep time, difficulty, cleanup level, ingredient visual icons). Include 5 main bottom navigation tabs: Home (Weekly meal plan with dish swap option), Kitchen (Pantry inventory & frozen leftovers tracking), Plan (Budget target, portion size, cooking days filter, cheapest week generator), Grocery (Interactive shopping list with store pricing and item check-offs), and You (Profile & preferences).

## Project Brief

# Project Brief: Budget Meal Planner (MVP)

## Features
1. **Weekly Meal Planner & Dish Swapping (Home & Plan)**: View and customize a weekly meal schedule tailored to target budgets, portion sizes, and cooking days, with options to swap dishes or generate cost-optimized meal plans.
2. **Pantry & Leftover Inventory Tracking (Kitchen)**: Track current pantry/kitchen inventory and frozen leftovers to prevent duplicate grocery purchases.
3. **Interactive Budget Grocery List (Grocery)**: Auto-generated shopping list featuring estimated store pricing, category grouping, and item check-offs to manage shopping budgets.
4. **Recipe Details & Cooking Effort**: View recipe cards with preparation time, difficulty rating, cleanup level, and visual ingredient icons.
5. **User Profile & Preferences (You)**: Configure personal preferences, dietary needs, default serving sizes, and budget thresholds.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Navigation**: Jetpack Navigation 3 (State-driven navigation)
- **Adaptive Layout Strategy**: Compose Material Adaptive library (`androidx.compose.material3.adaptive`)
- **Architecture & Concurrency**: Android Architecture Components (ViewModel, StateFlow) and Kotlin Coroutines

## Implementation Steps
**Total Duration:** 2h 10m 7s

### Task_1_SetupDataModelAndNavigation: Set up core data models, repositories with mock recipe/inventory/grocery data, and top-level 5-tab Navigation (Home, Plan, Grocery, Kitchen, You) using Jetpack Compose and Material 3.
- **Status:** COMPLETED
- **Updates:** Task_1_SetupDataModelAndNavigation completed. Core data models, mock repository, StateFlow state management, 5-tab Navigation 3 bar, and placeholder screens built and verified.
- **Acceptance Criteria:**
  - Core data models defined for Recipes, Meals, Grocery items, and Kitchen inventory
  - Mock repositories configured providing sample data
  - 5-tab bottom navigation bar functional with Material 3
  - build pass
- **Duration:** 5m 31s

### Task_2_ImplementHomeAndRecipeDetail: Implement Home Screen with weekly meal plan, dish swapping, cost per serving, and cooking time, plus Recipe Detail Screen featuring effort breakdown and ingredient visual cards.
- **Status:** COMPLETED
- **Updates:** Task_2_ImplementHomeAndRecipeDetail completed. Home screen displays weekly dinner plan, cost per serving, cook time, cuisine tags, and interactive dish swapping sheet. Recipe Detail screen displays header image, effort metrics (difficulty, items count, cleanup level), visual ingredient grid, instructions, and action buttons. All verified and passing tests.
- **Acceptance Criteria:**
  - Home screen displays weekly dinner plan with cost per serving and cooking time
  - Dish swapping mechanism working on Home screen
  - Recipe detail screen displays preparation time, difficulty, cleanup level, and ingredient cards
  - Navigation between Home and Recipe Detail working smoothly
  - build pass
- **Duration:** 3m 56s

### Task_3_ImplementPlanGroceryAndKitchen: Implement Plan Screen (budget, serving size, cooking days filter, cheapest plan generator), Grocery Screen (shopping list with checkboxes, store selector, category grouping, estimated total), and Kitchen Screen (pantry staples and frozen leftovers tracking).
- **Status:** COMPLETED
- **Updates:** Task_3_ImplementPlanGroceryAndKitchen completed. Implemented Plan screen (budget slider, servings stepper, days filter, dietary filters), Grocery screen (store selector, estimated total, aisle groupings, item check-off), and Kitchen screen (pantry tracking, leftovers, saved money UI). Data syncs properly across screens. Verified build.
- **Acceptance Criteria:**
  - Plan screen allows adjusting budget, serving size, cooking days, and generating cost-optimized plan
  - Grocery screen displays auto-generated shopping list with store selector, category grouping, item check-off, and estimated total
  - Kitchen screen tracks pantry items and frozen leftovers to avoid duplicate buying
  - Data is synchronized across Plan, Grocery, and Kitchen states
  - build pass
- **Duration:** 1h 52m 1s

### Task_4_YouScreenAndRunVerify: Implement You (User Profile & Preferences) screen and perform final Run and Verify to test application stability, confirm alignment with project brief, verify no crashes, and ensure all existing tests pass.
- **Status:** COMPLETED
- **Updates:** Task_4_YouScreenAndRunVerify completed. Implemented You Screen with Free/Pro subscription tiers (Monthly $2.99, Yearly $19.99, Lifetime $39.99), Free vs Pro feature breakdown, and settings toggles. Critic agent verified application stability on phone emulator. No crashes, all 5 tabs function correctly, and core features are intact with no critical UI layout issues. All existing tests pass.
- **Acceptance Criteria:**
  - You screen displays user profile, dietary preferences, default serving sizes, and budget thresholds
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - critic_agent verifies application stability, user requirements alignment, and UI layout quality
- **Duration:** 8m 39s

