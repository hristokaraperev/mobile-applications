package com.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.calorietracker.ui.navigation.CalorieTrackerNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Compose owns all navigation; this activity only sets the
 * content tree. Annotated with [AndroidEntryPoint] so Hilt can inject into
 * composables hosted here (via hiltViewModel).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalorieTrackerNavHost()
                }
            }
        }
    }
}
