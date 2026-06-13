package com.calorietracker.ui.foodsearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorietracker.data.food.FoodDto

/**
 * Food search screen: a debounced query field, a prominent "Scan barcode" entry
 * (wired in a later slice), the result list, and an "Add custom food" fallback.
 */
@Composable
fun FoodSearchScreen(
    onFoodSelected: (Long) -> Unit,
    onScanBarcode: () -> Unit,
    onAddCustomFood: () -> Unit,
    viewModel: FoodSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search foods") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = onScanBarcode, modifier = Modifier.fillMaxWidth()) {
            Text("Scan barcode")
        }

        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.results, key = { it.id }) { food ->
                FoodRow(food = food, onClick = { onFoodSelected(food.id) })
                HorizontalDivider()
            }
        }

        OutlinedButton(onClick = onAddCustomFood, modifier = Modifier.fillMaxWidth()) {
            Text("Add custom food")
        }
    }
}

@Composable
private fun FoodRow(food: FoodDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(food.name, style = MaterialTheme.typography.bodyLarge)
        val subtitle = listOfNotNull(
            food.brand,
            food.energyKcal?.let { "${it.toInt()} kcal / 100 g" },
        ).joinToString(" · ")
        if (subtitle.isNotEmpty()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
