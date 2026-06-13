package com.calorietracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.calorietracker.data.auth.DataStoreTokenStore
import com.calorietracker.data.auth.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Backing DataStore file for authentication preferences. */
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

/** Provides persistent storage dependencies. */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
        DataStoreTokenStore(context.authDataStore)
}
