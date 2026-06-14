package com.money.manager.ex.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getDatabasePath(): Flow<String?>
    suspend fun setDatabasePath(path: String?)
    fun getDatabaseName(): Flow<String?>
    suspend fun setDatabaseName(name: String?)
}
