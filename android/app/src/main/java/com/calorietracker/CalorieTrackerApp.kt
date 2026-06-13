package com.calorietracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Annotated with [HiltAndroidApp] so Hilt can generate
 * and attach the application-level dependency container.
 */
@HiltAndroidApp
class CalorieTrackerApp : Application()
