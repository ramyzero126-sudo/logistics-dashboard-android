
package com.example.logisticsdashboard.data

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "logistics_auth",
        Context.MODE_PRIVATE
    )

    fun getAccessToken(): String? =
        prefs.getString("access_token", null)

    fun saveTokens(accessToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .apply()
    }

    fun hasToken(): Boolean =
        !getAccessToken().isNullOrBlank()

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
