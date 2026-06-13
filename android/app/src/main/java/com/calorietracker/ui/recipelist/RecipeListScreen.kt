package com.calorietracker.ui.recipelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorietracker.data.recipe.RecipeDto

/**
 * Recipe list screen: every recipe the user owns with its per-portion kcal and actions
 * to create, edit, delete, or log a portion. Reloads on resume so a recipe saved in the
 * editor (or a portion logged) is reflected on return.
 */
@Composable
fun RecipeListScreen(
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    onLogPortion: (Long) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Recipes", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCreate) { Text("New recipe") }
        }

        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onEdit = { onEdit(recipe.id) },
                    onLogPortion = { onLogPortion(recipe.id) },
                    onDelete = { viewModel.delete(recipe.id) },
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: RecipeDto,
    onEdit: () -> Unit,
    onLogPortion: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${(recipe.perPortion.kcal ?: 0.0).toInt()} kcal / portion",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onLogPortion) { Text("Log a portion") }
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
