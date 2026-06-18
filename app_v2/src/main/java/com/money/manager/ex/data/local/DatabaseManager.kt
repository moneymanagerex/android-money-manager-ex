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
import android.database.sqlite.SQLiteDatabase

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

        patchEntireDatabaseSchema(file)

        val db = Room.databaseBuilder(
            context,
            MmexDatabase::class.java,
            file.absolutePath
        )
            // NON applicare nessuna modica sul DB. pittosto
            // crasch. il principio descirtto in @agent.md
            // prevede di NON modificare per nessun motivo
            // la struttura del db.
            // .fallbackToDestructiveMigrationOnDowngrade()
        .build()

        _database.value = db
    }

    private fun patchEntireDatabaseSchema(localDbFile: File) {
        // 1. Apriamo il database in modalità lettura/scrittura
        val db = SQLiteDatabase.openDatabase(
            localDbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        try {
            // 2. Sblocchiamo la scrittura sullo schema di sistema
            db.execSQL("PRAGMA writable_schema = 1;")

            // 3. Peschiamo tutte le tabelle create dall'utente (escludendo viste e trigger)
            val cursor = db.rawQuery(
                "SELECT name, sql FROM sqlite_master WHERE type='table' AND sql IS NOT NULL",
                null
            )

            // Prepariamo una lista per accumulare le modifiche da fare
            val updates = mutableListOf<Pair<String, String>>()

            // 4. Regex per trovare la parola "numeric" esatta, case-insensitive.
            // Il '\b' garantisce che sostituisca solo la parola intera e non rompa eventuali
            // nomi di colonne o tabelle che contengono quelle lettere (es. "is_numeric_value")
            val regex = "\\b(?i)numeric\\b".toRegex()

            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                val schemaSql = cursor.getString(1)

                // Se lo schema contiene 'numeric', lo patchiamo
                if (schemaSql.contains(regex)) {
                    val patchedSql = schemaSql.replace(regex, "REAL")
                    updates.add(Pair(tableName, patchedSql))
                }
            }
            cursor.close()

            // 5. Applichiamo tutte le modifiche salvate a sqlite_master
            for ((tableName, patchedSql) in updates) {
                val updateStmt = db.compileStatement(
                    "UPDATE sqlite_master SET sql = ? WHERE type='table' AND name = ?"
                )
                updateStmt.bindString(1, patchedSql)
                updateStmt.bindString(2, tableName)
                updateStmt.executeUpdateDelete()
                updateStmt.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 6. Rimettiamo il lucchetto e chiudiamo la connessione base
            db.execSQL("PRAGMA writable_schema = 0;")
            db.close()
        }
    }


    private fun patchDatabaseForExport(exportedDbFile: File) {
        // 1. Apriamo la copia del database pronta per l'export
        val db = SQLiteDatabase.openDatabase(
            exportedDbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        try {
            // 2. Sblocchiamo lo schema di sistema
            db.execSQL("PRAGMA writable_schema = 1;")

            // 3. Peschiamo tutte le tabelle create
            val cursor = db.rawQuery(
                "SELECT name, sql FROM sqlite_master WHERE type='table' AND sql IS NOT NULL",
                null
            )

            val updates = mutableListOf<Pair<String, String>>()

            // 4. Regex per trovare la parola esatta "REAL" (case-insensitive)
            val regex = "\\b(?i)REAL\\b".toRegex()

            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                val schemaSql = cursor.getString(1)

                // Se lo schema contiene 'REAL', lo riportiamo a 'numeric'
                if (schemaSql.contains(regex)) {
                    val patchedSql = schemaSql.replace(regex, "numeric")
                    updates.add(Pair(tableName, patchedSql))
                }
            }
            cursor.close()

            // 5. Applichiamo le modifiche di ripristino
            for ((tableName, patchedSql) in updates) {
                val updateStmt = db.compileStatement(
                    "UPDATE sqlite_master SET sql = ? WHERE type='table' AND name = ?"
                )
                updateStmt.bindString(1, patchedSql)
                updateStmt.bindString(2, tableName)
                updateStmt.executeUpdateDelete()
                updateStmt.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 6. Blocchiamo lo schema e chiudiamo
            db.execSQL("PRAGMA writable_schema = 0;")
            db.close()
        }
    }
}
