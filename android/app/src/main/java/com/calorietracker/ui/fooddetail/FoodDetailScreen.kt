package com.calorietracker.ui.fooddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorietracker.data.diary.MealType
import com.calorietracker.data.food.FoodDto
import java.time.LocalDate
import java.util.Locale

/**
 * Food detail screen: shows per-100 g nutrition, lets the user enter a quantity in
 * grams with a live kcal preview, pick a meal, and save the entry to the diary on
 * [entryDate]. Invokes [onSaved] once the entry is logged.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodDetailScreen(
    foodId: Long,
    mealType: MealType,
    entryDate: LocalDate,
    onSaved: () -> Unit,
    viewModel: FoodDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(foodId) {
        viewModel.load(foodId, mealType)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }

    val food = state.food
    if (state.isLoading || food == null) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                ?: CircularProgressIndicator()
        }

        return
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(food.name, style = MaterialTheme.typography.headlineSmall)
        NutritionPer100g(food)

        OutlinedTextField(
            value = state.quantityText,
            onValueChange = viewModel::onQuantityChange,
            label = { Text("Quantity (g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "${state.previewKcal.toInt()} kcal",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        MealPicker(selected = state.mealType, onSelect = viewModel::onMealTypeChange)

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { viewModel.save(entryDate) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add to diary")
        }
    }
}

@Composable
private fun NutritionPer100g(food: FoodDto) {
    Column {
        Text("Per 100 g", style = MaterialTheme.typography.titleSmall)
        food.energyKcal?.let { Text("Energy: ${it.toInt()} kcal") }
        food.proteinG?.let { Text("Protein: $it g") }
        food.carbsG?.let { Text("Carbs: $it g") }
        food.fatG?.let { Text("Fat: $it g") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealPicker(selected: MealType, onSelect: (MealType) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MealType.entries.forEach { meal ->
            FilterChip(
                selected = meal == selected,
                onClick = { onSelect(meal) },
                label = { Text(meal.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
            )
        }
    }
}
