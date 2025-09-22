package com.janad.parrot.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val REF_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val ACC_TOKEN_KEY = stringPreferencesKey("access_token")

        val EMAIL_KEY = stringPreferencesKey("email")

    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[REF_TOKEN_KEY] = token
        }
    }
      suspend fun saveAccessToken(token: String) {
            context.dataStore.edit { prefs ->
                prefs[ACC_TOKEN_KEY] = token
            }
        }

    // Get saved token
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACC_TOKEN_KEY]
    }
    suspend fun getAccessToken(): String? {
        return tokenFlow.first()
    }
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REF_TOKEN_KEY]
    }
    suspend fun getRefreshToken(): String? {
        return refreshTokenFlow.first()
    }
    // Clear login info (logout)
    suspend fun clearLoginInfo() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

fun UserPreferences.getAccessTokenBlocking(): String? = runBlocking { getAccessToken() }
fun UserPreferences.getRefreshTokenBlocking(): String? = runBlocking { getRefreshToken() }
