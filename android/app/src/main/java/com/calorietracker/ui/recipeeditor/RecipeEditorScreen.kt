package com.calorietracker.ui.recipeeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorietracker.data.recipe.NutritionDto

/**
 * Recipe editor: edits an existing recipe when [recipeId] is non-null, otherwise creates a
 * new one. The user names the recipe, sets a portion count, and adds ingredients via the
 * reused food search ([onAddIngredient]); [pickedFoodId] carries a food chosen there, for
 * which a grams prompt is shown. Total and per-portion nutrition update live. On save the
 * screen invokes [onSaved].
 */
@Composable
fun RecipeEditorScreen(
    recipeId: Long?,
    pickedFoodId: Long?,
    onPickedConsumed: () -> Unit,
    onAddIngredient: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.load(recipeId)
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }

    var pendingGrams by remember { mutableStateOf("100") }
    if (pickedFoodId != null) {
        AlertDialog(
            onDismissRequest = onPickedConsumed,
            title = { Text("Amount") },
            text = {
                OutlinedTextField(
                    value = pendingGrams,
                    onValueChange = { pendingGrams = it },
                    label = { Text("Grams") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingGrams.toDoubleOrNull()?.let { viewModel.addIngredientById(pickedFoodId, it) }
                    pendingGrams = "100"
                    onPickedConsumed()
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = onPickedConsumed) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Recipe name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.portionsText,
            onValueChange = viewModel::onPortionsChange,
            label = { Text("Number of portions") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        NutritionSummary(total = state.total, perPortion = state.perPortion)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onAddIngredient) { Text("Add ingredient") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(state.ingredients) { index, ingredient ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${ingredient.food.name} · ${ingredient.grams.toInt()} g")
                        TextButton(onClick = { viewModel.removeIngredient(index) }) { Text("Remove") }
                    }
                }
            }
        }

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save recipe")
        }
    }
}

@Composable
private fun NutritionSummary(total: NutritionDto, perPortion: NutritionDto) {
    Column {
        Text(
            text = "${(perPortion.kcal ?: 0.0).toInt()} kcal / portion",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Total: ${(total.kcal ?: 0.0).toInt()} kcal",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
