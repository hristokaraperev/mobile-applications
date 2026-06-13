package com.calorietracker.ui.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calorietracker.data.diary.MealType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateLabelFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

/**
 * Home/Diary screen: a date selector, the four meal sections with their entries
 * and per-meal kcal, and a daily total against the user's goal. Tapping a meal's
 * "+" opens Food search for that meal and date via [onAddFood]; the app bar's
 * profile action opens the Profile screen via [onOpenProfile].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onAddFood: (MealType, LocalDate) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRecipes: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Reload on every resume so entries logged on the detail screen appear on return.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load(state.date)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diary") },
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Text("⚙", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            DateSelector(
                date = state.date,
                onPrevious = viewModel::previousDay,
                onNext = viewModel::nextDay,
            )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onOpenRecipes) { Text("Recipes") }
        }

        DateSelector(
            date = state.date,
            onPrevious = viewModel::previousDay,
            onNext = viewModel::nextDay,
        )

            DailyTotal(totalKcal = state.totalKcal, goal = state.dailyKcalGoal)

            state.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.meals, key = { it.mealType }) { section ->
                    MealCard(section = section, onAdd = { onAddFood(section.mealType, state.date) })
                }
            }
        }
    }
}

@Composable
private fun DateSelector(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Text("‹", style = MaterialTheme.typography.headlineSmall)
        }
        Text(date.format(dateLabelFormat), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun DailyTotal(totalKcal: Double, goal: Int?) {
    val label = if (goal != null) {
        "${totalKcal.toInt()} / $goal kcal"
    } else {
        "${totalKcal.toInt()} kcal"
    }

    Text(
        text = label,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
}

@Composable
private fun MealCard(section: MealSection, onAdd: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mealLabel(section.mealType),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${section.kcal.toInt()} kcal", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = onAdd) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            section.entries.forEach { entry ->
                HorizontalDivider()
                Text(
                    text = "${entry.quantity.toInt()} g · ${entry.kcal.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

private fun mealLabel(mealType: MealType): String =
    mealType.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
