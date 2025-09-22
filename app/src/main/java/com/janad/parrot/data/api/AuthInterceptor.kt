package com.janad.parrot.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?,            // get current access token
    private val refreshTokenProvider: () -> String?,     // get current refresh token
    private val onNewAccessToken: (String) -> Unit,      // save new access token in ViewModel/UserPreferences
    private val refreshCall: (String) -> retrofit2.Call<RefreshRes> // a blocking call to refresh token
) : Interceptor {
    private val refreshLock = Any()
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // add token normally (no synchronized)
        val token = tokenProvider()
        val authRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else originalRequest

        var response = chain.proceed(authRequest)

        if (response.code == 401 || response.code == 403) {
            response.close()
            val refreshToken = refreshTokenProvider()
            if (!refreshToken.isNullOrEmpty()) {
                synchronized(refreshLock) {  // only lock refresh section
                    val newToken = try {
                        val refreshResponse = refreshCall(refreshToken).execute()
                        refreshResponse.body()?.accessToken ?: ""
                    } catch (_: Exception) { "" }

                    if (newToken.isNotEmpty()) {
                        onNewAccessToken(newToken)
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(newRequest)
                    }
                }
            }
        }
        return response
    }
}

