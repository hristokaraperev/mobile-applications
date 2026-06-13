package com.calorietracker.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Profile/settings screen: shows the signed-in user's email, lets them edit their
 * daily kcal goal, displays data-source attribution, and offers a logout action.
 *
 * @param onBack invoked when the user dismisses the screen via the app bar.
 * @param onLoggedOut invoked once logout has cleared the token, to return to Login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLoggedOut()
        }
    }

    var goalText by rememberSaveable(state.dailyKcalGoal) {
        mutableStateOf(state.dailyKcalGoal?.toString().orEmpty())
    }
    val parsedGoal = goalText.toIntOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineSmall)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.email?.let { email ->
                Text("Signed in as", style = MaterialTheme.typography.labelMedium)
                Text(email, style = MaterialTheme.typography.titleMedium)
            }

            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                label = { Text("Daily kcal goal") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = goalText.isNotEmpty() && (parsedGoal == null || parsedGoal <= 0),
                modifier = Modifier.fillMaxWidth(),
            )

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { parsedGoal?.let(viewModel::saveGoal) },
                enabled = !state.isSaving && parsedGoal != null && parsedGoal > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save goal")
            }

            DataAttribution()

            OutlinedButton(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log out")
            }
        }
    }
}

/** Data-source attribution required by the licences of the nutrition datasets. */
@Composable
private fun DataAttribution() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Data sources", style = MaterialTheme.typography.titleSmall)
        Text(
            "Food data from Open Food Facts, available under the Open Database " +
                "Licence (ODbL).",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            "Nutrition reference data from ANSES-CIQUAL.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
