package com.calorietracker.data.auth

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the stored JWT as a `Authorization: Bearer <token>` header to every
 * outgoing request. When no token is available (e.g. before login) the request
 * is sent unchanged.
 *
 * The token is supplied lazily via [tokenProvider] so the interceptor reads the
 * current value on each call rather than capturing a stale token.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request()
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authorized = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authorized)
    }
}
