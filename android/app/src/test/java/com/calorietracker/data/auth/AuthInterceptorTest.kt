package com.calorietracker.data.auth

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun clientWithToken(token: String?): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { token })
            .build()

    @Test
    fun `attaches bearer authorization header when a token is present`() {
        server.enqueue(MockResponse())
        val client = clientWithToken("jwt-token-123")

        client.newCall(Request.Builder().url(server.url("/diary")).build()).execute()

        val recorded = server.takeRequest()
        assertEquals("Bearer jwt-token-123", recorded.getHeader("Authorization"))
    }

    @Test
    fun `sends no authorization header when no token is available`() {
        server.enqueue(MockResponse())
        val client = clientWithToken(null)

        client.newCall(Request.Builder().url(server.url("/diary")).build()).execute()

        val recorded = server.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
    }
}
