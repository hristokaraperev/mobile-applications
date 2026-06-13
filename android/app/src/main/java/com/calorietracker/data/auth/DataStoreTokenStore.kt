package com.calorietracker.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [TokenStore] backed by Jetpack DataStore (Preferences). The token is stored
 * under a single key in an app-private preferences file.
 */
class DataStoreTokenStore(
    private val dataStore: DataStore<Preferences>,
) : TokenStore {

    override val token: Flow<String?> =
        dataStore.data.map { preferences -> preferences[TOKEN_KEY] }

    override suspend fun currentToken(): String? = token.first()

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences -> preferences[TOKEN_KEY] = token }
    }

    override suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(TOKEN_KEY) }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt")
    }
}
