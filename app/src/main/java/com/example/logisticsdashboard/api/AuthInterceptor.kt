package com.example.logisticsdashboard.api

import com.example.logisticsdashboard.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = tokenManager.getAccessToken()

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        // لو رجع 401 → نمسح التوكن
        if (response.code == 401) {
            tokenManager.clearTokens()
        }

        return response
    }
}
