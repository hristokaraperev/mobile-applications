package com.calorietracker.data.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TokenStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    private fun newStore(): DataStoreTokenStore {
        val dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            File(tmp.newFolder(), "auth.preferences_pb")
        }

        return DataStoreTokenStore(dataStore)
    }

    @Test
    fun `returns the token that was saved`() = runTest {
        val store = newStore()

        store.saveToken("jwt-abc")

        assertEquals("jwt-abc", store.currentToken())
    }

    @Test
    fun `has no token before anything is saved`() = runTest {
        val store = newStore()

        assertNull(store.currentToken())
    }

    @Test
    fun `clear removes the stored token`() = runTest {
        val store = newStore()
        store.saveToken("jwt-abc")

        store.clear()

        assertNull(store.currentToken())
    }
}
