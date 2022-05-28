package com.nt118.joliecafeadmin.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nt118.joliecafeadmin.util.Constants.Companion.PREFERENCES_ADMIN_TOKEN
import com.nt118.joliecafeadmin.util.Constants.Companion.PREFERENCES_BACK_ONLINE
import com.nt118.joliecafeadmin.util.Constants.Companion.PREFERENCES_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_NAME
)

@ViewModelScoped
class DataStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object PreferenceKeys {
        val backOnline = booleanPreferencesKey(PREFERENCES_BACK_ONLINE)
        val userToken = stringPreferencesKey(PREFERENCES_ADMIN_TOKEN)
    }

    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun saveBackOnline(backOnline: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.backOnline] = backOnline
        }
    }

    suspend fun saveAdminToken(userToken: String) {
        Log.d("get", "Save token")
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.userToken] = userToken
        }
    }


    val readBackOnline: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val backOnline = preferences[PreferenceKeys.backOnline] ?: false
            backOnline
        }.distinctUntilChanged()

    val readAdminToken: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            println("readtoken")
            val userToken = preferences[PreferenceKeys.userToken] ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjYyOTBkMmY0OTY1MTBjZWNhYzA3MzBjOCIsImlhdCI6MTY1MzY1OTA0MH0.lkHuEWIn8HwF-N8tMq0_C8FZdU4CuPAGLfM7hnDOCEM"
            userToken
        }.distinctUntilChanged()

}