package com.calorietracker.ui.logportion

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorietracker.data.diary.MealType
import java.time.LocalDate
import java.util.Locale

/**
 * Log-a-portion screen: loads a recipe's per-portion kcal, lets the user choose a meal and
 * a number of portions (e.g. 0.5, 1, 2) with a live kcal preview, and logs the portion to
 * the diary on [entryDate]. Invokes [onLogged] once the entry is created.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogPortionScreen(
    recipeId: Long,
    entryDate: LocalDate,
    onLogged: () -> Unit,
    viewModel: LogPortionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipeId) {
        viewModel.load(recipeId)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onLogged()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(state.recipeName, style = MaterialTheme.typography.headlineSmall)
        Text("${state.perPortionKcal.toInt()} kcal / portion", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = state.quantityText,
            onValueChange = viewModel::onQuantityChange,
            label = { Text("Portions") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            Text("Log a portion")
        }
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
