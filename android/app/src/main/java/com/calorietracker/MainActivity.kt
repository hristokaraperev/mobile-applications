package com.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
 *
 * Draws edge-to-edge: [enableEdgeToEdge] lets content extend behind the system
 * bars so the bars are transparent. Each screen is then responsible for keeping
 * its content inside the safe area via window insets (Scaffold content insets for
 * the normal screens, [androidx.compose.foundation.layout.safeDrawingPadding] on
 * the scanner overlay).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
