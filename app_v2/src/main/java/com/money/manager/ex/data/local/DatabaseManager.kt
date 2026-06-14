package com.money.manager.ex.data.local

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.money.manager.ex.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val _database = MutableStateFlow<MmexDatabase?>(null)
    val database: StateFlow<MmexDatabase?> = _database.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            settingsRepository.getDatabasePath().collectLatest { uriString ->
                if (uriString != null) {
                    try {
                        val uri = Uri.parse(uriString)
                        val localFile = copyUriToLocal(uri)
                        if (localFile != null) {
                            rebuildDatabase(localFile)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun copyUriToLocal(uri: Uri): File? {
        return try {
            val localFile = File(context.filesDir, "active_database.db")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }
            localFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rebuildDatabase(file: File) {
        // Close existing database if any
        _database.value?.close()

        val db = Room.databaseBuilder(
            context,
            MmexDatabase::class.java,
            file.absolutePath
        )
        .fallbackToDestructiveMigration() // In development, match legacy schema later
        .build()

        _database.value = db
    }
}
