package com.example.afapp.data.local.storage


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore("asfaltofashion_prefs")

class UserPreferences(context: Context) {

    private val appContext = context.applicationContext

    val isLoggedIn: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_LOGGED_IN] ?: false
    }

    val lastEmail: Flow<String?> = appContext.dataStore.data.map {
        it[KEY_LAST_EMAIL]
    }

    val theme: Flow<String> = appContext.dataStore.data.map {
        it[KEY_THEME] ?: "system"
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        appContext.dataStore.edit {
            it[KEY_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun saveLogin(email: String) {
        appContext.dataStore.edit {
            it[KEY_LOGGED_IN] = true
            it[KEY_LAST_EMAIL] = email
        }
    }

    suspend fun clearLogin() {
        appContext.dataStore.edit {
            it.remove(KEY_LOGGED_IN)
            it.remove(KEY_LAST_EMAIL)
        }
    }

    suspend fun saveTheme(theme: String) {
        appContext.dataStore.edit {
            it[KEY_THEME] = theme
        }
    }

    companion object {
        private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        private val KEY_LAST_EMAIL = stringPreferencesKey("last_email")
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}