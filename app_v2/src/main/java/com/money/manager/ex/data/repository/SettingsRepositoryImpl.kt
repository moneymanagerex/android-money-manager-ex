package com.money.manager.ex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.money.manager.ex.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "mmex_v2_settings",
        Context.MODE_PRIVATE
    )

    private companion object {
        const val KEY_DATABASE_PATH = "database_path"
        const val KEY_DATABASE_NAME = "database_name"
    }

    override fun getDatabasePath(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_DATABASE_PATH) {
                trySend(prefs.getString(KEY_DATABASE_PATH, null))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPreferences.getString(KEY_DATABASE_PATH, null))
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setDatabasePath(path: String?) {
        sharedPreferences.edit().putString(KEY_DATABASE_PATH, path).apply()
    }

    override fun getDatabaseName(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_DATABASE_NAME) {
                trySend(prefs.getString(KEY_DATABASE_NAME, null))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPreferences.getString(KEY_DATABASE_NAME, null))
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setDatabaseName(name: String?) {
        sharedPreferences.edit().putString(KEY_DATABASE_NAME, name).apply()
    }
}
