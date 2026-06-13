package com.calorietracker.ui.customfood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Custom-food form reached when a scanned barcode is unknown. The barcode is pre-filled
 * and shown read-only; the user supplies a name and per-100 g nutrition, then saves.
 * Once the food is created, [onCreated] is invoked with its id so the caller can open
 * its detail screen.
 *
 * @param barcode the scanned EAN to pre-fill, or empty when reached without a scan.
 */
@Composable
fun CustomFoodScreen(
    barcode: String,
    onCreated: (Long) -> Unit,
    viewModel: CustomFoodViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(barcode) {
        viewModel.prefill(barcode)
    }

    LaunchedEffect(state.createdFoodId) {
        state.createdFoodId?.let(onCreated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Add custom food", style = MaterialTheme.typography.headlineSmall)

        if (state.barcode.isNotBlank()) {
            Text(
                "Barcode: ${state.barcode}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.brand,
            onValueChange = viewModel::onBrandChange,
            label = { Text("Brand (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.energyKcalText,
            onValueChange = viewModel::onEnergyKcalChange,
            label = { Text("Energy (kcal / 100 g)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.proteinText,
            onValueChange = viewModel::onProteinChange,
            label = { Text("Protein (g / 100 g, optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.carbsText,
            onValueChange = viewModel::onCarbsChange,
            label = { Text("Carbs (g / 100 g, optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.fatText,
            onValueChange = viewModel::onFatChange,
            label = { Text("Fat (g / 100 g, optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save food")
        }
    }
}
