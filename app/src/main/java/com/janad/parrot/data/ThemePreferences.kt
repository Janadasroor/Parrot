package com.janad.parrot.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.janad.parrot.data.models.ui.ThemeSetting
import kotlinx.coroutines.flow.map

object ThemePreferences {
        private val THEME_KEY = stringPreferencesKey("theme_setting")

        suspend fun saveTheme(context: Context, theme: ThemeSetting) {
            context.dataStore.edit { it[THEME_KEY] = theme.name }
        }

        fun loadTheme(context: Context) = context.dataStore.data.map { prefs ->
            ThemeSetting.valueOf(prefs[THEME_KEY] ?: ThemeSetting.SYSTEM.name)
        }
    }