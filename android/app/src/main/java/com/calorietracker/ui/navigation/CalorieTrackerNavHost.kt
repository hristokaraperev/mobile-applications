package com.calorietracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.calorietracker.data.diary.MealType
import com.calorietracker.ui.auth.LoginScreen
import com.calorietracker.ui.auth.RegisterScreen
import com.calorietracker.ui.diary.DiaryScreen
import com.calorietracker.ui.fooddetail.FoodDetailScreen
import com.calorietracker.ui.foodsearch.FoodSearchScreen
import com.calorietracker.ui.logportion.LogPortionScreen
import com.calorietracker.ui.recipeeditor.RecipeEditorScreen
import com.calorietracker.ui.recipelist.RecipeListScreen
import java.time.LocalDate

/**
 * Top-level navigation graph. Starts at Login; on successful authentication the
 * back stack is reset to the Diary destination so the user cannot navigate back
 * into the auth flow. From the diary the user drills into food search and detail,
 * carrying the target meal and date forward so a saved entry lands on the right day.
 */
@Composable
fun CalorieTrackerNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onAuthenticated = { navController.toDiary() },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onAuthenticated = { navController.toDiary() },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.DIARY) {
            DiaryScreen(
                onAddFood = { mealType, date ->
                    navController.navigate(Routes.foodSearch(mealType, date))
                },
                onOpenRecipes = { navController.navigate(Routes.RECIPES) },
            )
        }
        composable(
            route = Routes.FOOD_SEARCH,
            arguments = listOf(
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
            ),
        ) { entry ->
            val mealType = entry.mealType()
            val date = entry.date()

            FoodSearchScreen(
                onFoodSelected = { foodId ->
                    navController.navigate(Routes.foodDetail(foodId, mealType, date))
                },
                onScanBarcode = { /* Barcode scanner is added in a later slice. */ },
                onAddCustomFood = { /* Custom-food creation is added in a later slice. */ },
            )
        }
        composable(
            route = Routes.FOOD_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_FOOD_ID) { type = NavType.LongType },
                navArgument(Routes.ARG_MEAL_TYPE) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) { type = NavType.StringType },
            ),
        ) { entry ->
            FoodDetailScreen(
                foodId = entry.arguments?.getLong(Routes.ARG_FOOD_ID) ?: 0L,
                mealType = entry.mealType(),
                entryDate = entry.date(),
                onSaved = { navController.popBackStack(Routes.DIARY, inclusive = false) },
            )
        }
        composable(Routes.RECIPES) {
            RecipeListScreen(
                onCreate = { navController.navigate(Routes.RECIPE_EDITOR_NEW) },
                onEdit = { recipeId -> navController.navigate(Routes.recipeEditor(recipeId)) },
                onLogPortion = { recipeId -> navController.navigate(Routes.logPortion(recipeId)) },
            )
        }
        composable(Routes.RECIPE_EDITOR_NEW) { entry ->
            val pickedFoodId by entry.savedStateHandle
                .getStateFlow<Long?>(Routes.RESULT_INGREDIENT_FOOD_ID, null)
                .collectAsStateWithLifecycle()

            RecipeEditorScreen(
                recipeId = null,
                pickedFoodId = pickedFoodId,
                onPickedConsumed = { entry.savedStateHandle[Routes.RESULT_INGREDIENT_FOOD_ID] = null },
                onAddIngredient = { navController.navigate(Routes.INGREDIENT_SEARCH) },
                onSaved = { navController.popBackStack(Routes.RECIPES, inclusive = false) },
            )
        }
        composable(
            route = Routes.RECIPE_EDITOR_EDIT,
            arguments = listOf(navArgument(Routes.ARG_RECIPE_ID) { type = NavType.LongType }),
        ) { entry ->
            val pickedFoodId by entry.savedStateHandle
                .getStateFlow<Long?>(Routes.RESULT_INGREDIENT_FOOD_ID, null)
                .collectAsStateWithLifecycle()

            RecipeEditorScreen(
                recipeId = entry.arguments?.getLong(Routes.ARG_RECIPE_ID),
                pickedFoodId = pickedFoodId,
                onPickedConsumed = { entry.savedStateHandle[Routes.RESULT_INGREDIENT_FOOD_ID] = null },
                onAddIngredient = { navController.navigate(Routes.INGREDIENT_SEARCH) },
                onSaved = { navController.popBackStack(Routes.RECIPES, inclusive = false) },
            )
        }
        composable(Routes.INGREDIENT_SEARCH) {
            FoodSearchScreen(
                onFoodSelected = { foodId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.RESULT_INGREDIENT_FOOD_ID, foodId)
                    navController.popBackStack()
                },
                onScanBarcode = { /* Barcode scanner is added in a later slice. */ },
                onAddCustomFood = { /* Custom-food creation is added in a later slice. */ },
            )
        }
        composable(
            route = Routes.LOG_PORTION,
            arguments = listOf(navArgument(Routes.ARG_RECIPE_ID) { type = NavType.LongType }),
        ) { entry ->
            LogPortionScreen(
                recipeId = entry.arguments?.getLong(Routes.ARG_RECIPE_ID) ?: 0L,
                entryDate = LocalDate.now(),
                onLogged = { navController.popBackStack(Routes.RECIPES, inclusive = false) },
            )
        }
    }
}

/** Navigates to Diary, clearing the auth flow from the back stack. */
private fun NavHostController.toDiary() {
    navigate(Routes.DIARY) {
        popUpTo(Routes.LOGIN) { inclusive = true }
        launchSingleTop = true
    }
}

private fun androidx.navigation.NavBackStackEntry.mealType(): MealType =
    MealType.valueOf(arguments?.getString(Routes.ARG_MEAL_TYPE) ?: MealType.BREAKFAST.name)

private fun androidx.navigation.NavBackStackEntry.date(): LocalDate =
    LocalDate.parse(arguments?.getString(Routes.ARG_DATE) ?: LocalDate.now().toString())
