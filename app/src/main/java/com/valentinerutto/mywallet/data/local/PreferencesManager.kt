package com.valentinerutto.mywallet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "banking_prefs")

class PreferencesManager(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val CUSTOMER_ID = stringPreferencesKey("customer_id")
    }
    
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    val customerId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[CUSTOMER_ID]
    }
    
    suspend fun setLoggedIn(isLoggedIn: Boolean, customerId: String? = null) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
            if (customerId != null) {
                preferences[CUSTOMER_ID] = customerId
            }
        }
    }
    
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
