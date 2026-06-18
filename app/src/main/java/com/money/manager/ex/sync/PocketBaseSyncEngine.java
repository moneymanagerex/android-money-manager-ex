package com.money.manager.ex.sync;

import android.content.Context;
import android.database.Cursor;
//import android.database.sqlite.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.database.SQLException;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.utils.MmxFileUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Core engine for PocketBase synchronization.
 * Handles mapping, push/pull logic and sync orchestration according to table_config.json.
 */
public class PocketBaseSyncEngine {

    private final Context mContext;
    private final Gson mGson;
    private SyncConfig mConfig;
    private final PocketBaseApiService mService;

    @Inject
    Lazy<MmxOpenHelper> openHelper;

    public interface SyncProgressListener {
        void onProgress(String tableName, String action);
    }

    public static class SyncConfig {
        public Map<String, TableConfig> SYNC_CONFIG;
        public List<String> SYNC_ORDER;
    }

    public static class TableConfig {
        public String pk;
        public List<String> fields;
    }

    public PocketBaseSyncEngine(Context context) {
        mContext = context;
        mGson = new Gson();
        mService = PocketBaseClient.getInstance(context).getService();

        // Inject dependencies (Dagger 2)
        MmexApplication.getApp().iocComponent.inject(this);

        loadConfig();
    }

    private void loadConfig() {
        try {
            int resId = mContext.getResources().getIdentifier("table_config", "raw", mContext.getPackageName());
            String json = MmxFileUtils.getRawAsString(mContext, resId);
            mConfig = mGson.fromJson(json, SyncConfig.class);
        } catch (Exception e) {
            Timber.e(e, "[SYNC_CLOUD] Error loading table_config.json");
        }
    }

    /**
     * Orchestrates the sync cycle: Push followed by Pull.
     */
    public void synchronize() {
        synchronize(null);
    }

    /**
     * Orchestrates the sync cycle: Push followed by Pull with progress reporting.
     */
    public void synchronize(SyncProgressListener listener) {
        if (!SyncManager.isCloudSyncEnabled() || mConfig == null) return;

        Timber.d("[SYNC_CLOUD] start sync");
        SupportSQLiteDatabase db = openHelper.get().getWritableDatabase();

        if (db == null || !db.isOpen() ) {
            return;
        }

        try {
            // Use exclusive transaction for the whole sync cycle
            db.beginTransaction();

            // 1. Push local changes
            for (String tableName : mConfig.SYNC_ORDER) {
                if (listener != null) listener.onProgress(tableName, "Pushing");
                pushTableChanges(db, tableName);
            }

            // 2. Pull remote changes
            // this is wrong becouse ise local time with Z...
            // String syncStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.US).format(new Date());
            String syncStartTime = java.time.Instant.now().toString();

            SyncPreferences prefs = new SyncPreferences(mContext);
            String lastSync = prefs.getPocketBaseSyncLastSyncTime();
            // go back 5 seconds for buffering
            // wrong way
            // lastSync = java.time.Instant.parse(lastSync.replace(" ", "T")).minusSeconds(5).toString().replace("T", " ");
            //if (lastSync.contains(" ")) {
            //    // Trasforma il vecchio "2026-05-17 16:30:00.123" in "2026-05-17T16:30:00.123Z"
            //    lastSync = lastSync.replace(" ", "T");
            //}
            try {
                lastSync = java.time.Instant.parse(lastSync)
                        .minusSeconds(5)
                        .toString();
            } catch (Exception ignored) {
            }
            boolean hasError = false;

            for (String tableName : mConfig.SYNC_ORDER) {
                Timber.d("Download table %s", tableName);
                if (listener != null) listener.onProgress(tableName, "Pulling");
                hasError = hasError || !pullTableChanges(db, tableName, lastSync);
            }
            db.setTransactionSuccessful();
            if (!hasError) prefs.setPocketBaseSyncLastSyncTime(syncStartTime);
            // Timber.i("[SYNC_CLOUD] Synchronization cycle completed.");
        } catch (Exception e) {
            Timber.e(e, "[SYNC_CLOUD] Error during synchronization");
        } finally {
            db.endTransaction();
        }
        try {
            db.close();
        } catch (IOException e) {
            Timber.e(e, "[SYNC_CLOUD] Error closing database");
        }

    }

    private void pushTableChanges(SupportSQLiteDatabase db, String tableName) throws IOException {
        TableConfig config = mConfig.SYNC_CONFIG.get(tableName);
        if (config == null) return;

        // Handle Deletions first
        processTableDeletions(db, tableName);

        // Handle Dirty records
        String selection = "pb_is_dirty = 1 OR pb_id IS NULL OR pb_id = ''";
        Cursor cursor = db.query("SELECT * FROM " + tableName + " WHERE " + selection);

        try {
            int pkIdx = cursor.getColumnIndex(config.pk);
            int pbIdIdx = cursor.getColumnIndex("pb_id");

            while (cursor.moveToNext()) {
                if (pkIdx == -1) continue;

                JsonObject payload = new JsonObject();
                payload.addProperty(config.pk, cursor.getString(pkIdx));
                // Nota: Assicurati che pb_updated_at sia una stringa ISO o un formato accettato
                int updatedAtIdx = cursor.getColumnIndex("pb_updated_at");
                if (updatedAtIdx != -1) {
                    payload.addProperty("_updated_at", cursor.getString(updatedAtIdx));
                }

                for (String field : config.fields) {
                    int colIdx = cursor.getColumnIndex(field);
                    if (colIdx != -1 && !cursor.isNull(colIdx)) {
                        payload.addProperty(field, cursor.getString(colIdx));
                    }
                }

                String currentPbId = pbIdIdx != -1 ? cursor.getString(pbIdIdx) : null;
                long localId = cursor.getLong(pkIdx);
                Response<JsonObject> response = null;

                try {
                    if (currentPbId == null || TextUtils.isEmpty(currentPbId)) {
                        // CASO 1: Nessun ID remoto -> CREA
                        response = mService.create(tableName, payload).execute();
                    } else {
                        // CASO 2: ID presente -> UPDATE
                        response = mService.update(tableName, currentPbId, payload).execute();

                        // FALLBACK: Se l'update fallisce con 404, il record è sparito dal server
                        if (!response.isSuccessful() && response.code() == 404) {
                            // Aggiungiamo l'ID al payload per tentare di ricrearlo con lo stesso ID
                            payload.addProperty("id", currentPbId);
                            response = mService.create(tableName, payload).execute();
                        }
                    }

                    // Verifica finale del risultato (sia da Create che da Update/Fallback)
                    if (response.isSuccessful() && response.body() != null) {
                        String remoteId = response.body().get("id").getAsString();
                        updateLocalRecordAfterPush(db, tableName, config.pk, localId, remoteId);
                    } else {
                        // Gestione errori avanzata
                        handlePushError(db, tableName, config, cursor, response, payload, localId, currentPbId);
                    }

                } catch (IOException e) {
                    Timber.e(e, "[SYNC_CLOUD] Network error pushing record %d in %s", localId, tableName);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }

    }

    private boolean pullTableChanges(SupportSQLiteDatabase db, String tableName, String lastSync) throws IOException {
        TableConfig config = mConfig.SYNC_CONFIG.get(tableName);
        if (config == null) return false;

        int page = 1;
        int totalPages = 1;

        boolean hasError = false;

        while (page <= totalPages) {
            Map<String, String> options = new HashMap<>();
            options.put("filter", "_updated_at > \"" + lastSync + "\"");
            options.put("sort", "_updated_at");
            options.put("page", String.valueOf(page));
            options.put("perPage", "200"); // Aumentiamo il limite per pagina a 200

            Response<JsonObject> response = mService.getRecords(tableName, options).execute();
            if (!response.isSuccessful() || response.body() == null) break;

            JsonObject body = response.body();
            totalPages = body.get("totalPages").getAsInt();
            com.google.gson.JsonArray items = body.getAsJsonArray("items");

            for (JsonElement itemElement : items) {
                JsonObject rmt = itemElement.getAsJsonObject();
                if (!applyRemoteRecord(db, tableName, config, rmt, false)) {
                    hasError = true;
                }
            }
            page++;
        }

        // Reset pb_is_dirty to 0 for all pull-updated records
        db.execSQL("UPDATE " + tableName + " SET pb_is_dirty = 0 WHERE pb_is_dirty = 2");
        return !hasError;
    }

    private void handlePushError(SupportSQLiteDatabase db, String tableName, TableConfig config, Cursor cursor,
                                 Response<JsonObject> response, JsonObject payload, long localId, String currentPbId) throws IOException {

        // 409 Conflict: record modified on server
        if (response.code() == 409) {
            if (!TextUtils.isEmpty(currentPbId)) {
                Response<JsonObject> getResponse = mService.getOne(tableName, currentPbId).execute();
                if (getResponse.isSuccessful() && getResponse.body() != null) {
                    applyRemoteRecord(db, tableName, config, getResponse.body(), true);
                    Timber.d("[SYNC_CLOUD] Resolved 409 conflict for %s (id: %d)", tableName, localId);
                }
            }
            return;
        }

        // Unique Constraint Violation
        String errorBody = null;
        try (okhttp3.ResponseBody errorResponseBody = response.errorBody()) {
            if (errorResponseBody != null) {
                errorBody = errorResponseBody.string();
            }
        }

        if (isUniqueValidationError(errorBody)) {
            JsonObject remoteRecord = null;
            if ("TAGLINK_V1".equals(tableName)) {
                int reftypeIdx = cursor.getColumnIndex("REFTYPE");
                int refidIdx = cursor.getColumnIndex("REFID");
                int tagidIdx = cursor.getColumnIndex("TAGID");

                if (reftypeIdx != -1 && refidIdx != -1 && tagidIdx != -1) {
                    String reftype = cursor.getString(reftypeIdx);
                    long refid = cursor.getLong(refidIdx);
                    long tagid = cursor.getLong(tagidIdx);
                    String filter = String.format(Locale.US, "REFTYPE='%s' && REFID=%d && TAGID=%d", reftype, refid, tagid);
                    remoteRecord = getRemoteRecord(tableName, filter);
                }
            } else {
                // Search by primary key (config.pk)
                String filter = String.format(Locale.US, "%s=%d", config.pk, localId);
                remoteRecord = getRemoteRecord(tableName, filter);
            }

            if (remoteRecord != null) {
                String remoteId = remoteRecord.get("id").getAsString();
                // Update local record with remote ID
                updateLocalRecordAfterPushKeepingDirty(db, tableName, config.pk, localId, remoteId);
                // next cicle update correctly

                // Retry update. We don't' retry now because 409  can be raised. next cycle
/*                Response<JsonObject> retryResponse = mService.update(tableName, remoteId, payload).execute();
                if (retryResponse.isSuccessful() && retryResponse.body() != null) {
                    updateLocalRecordAfterPush(db, tableName, config.pk, localId, remoteId);
                    Timber.d("[SYNC_CLOUD] Resolved unique conflict for %s (id: %d) by linking to remote record", tableName, localId);
                } */
            }
        } else {
            Timber.e("[SYNC_CLOUD] Error pushing record %d in %s: %d %s", localId, tableName, response.code(), errorBody);
        }
    }

    private boolean applyRemoteRecord(SupportSQLiteDatabase db, String tableName, TableConfig config, JsonObject rmt, boolean markAsSynced) {
        String pbId = rmt.get("id").getAsString();
        String updatedAt = rmt.get("_updated_at").getAsString();

        // Check if deleted in remote
        if (rmt.has("_is_deleted") && rmt.get("_is_deleted").getAsInt() == 1) {
            db.execSQL("DELETE FROM " + tableName + " WHERE pb_id = ?", new Object[]{pbId});
            return true;
        }

        // Check if exists locally
        Cursor localCursor = db.query("SELECT " + config.pk + " FROM " + tableName + " WHERE pb_id = ?", new Object[]{pbId});
        boolean existsLocally = localCursor != null && localCursor.moveToFirst();
        if (localCursor != null) localCursor.close();

        int dirtyValue = markAsSynced ? 0 : 2;

        if (existsLocally) {
            // UPDATE locally
            StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
            Object[] values = new Object[config.fields.size() + 3];
            int i = 0;
            for (String field : config.fields) {
                sql.append(field).append(" = ?, ");
                values[i++] = rmt.has(field) && !rmt.get(field).isJsonNull() ? rmt.get(field).getAsString() : null;
            }
            sql.append("pb_updated_at = ?, pb_is_dirty = ? WHERE pb_id = ?");
            values[i++] = updatedAt;
            values[i++] = dirtyValue;
            values[i] = pbId;
            try {
                db.execSQL(sql.toString(), values);
                return true;
            } catch (SQLException e) {
                Timber.e(e, "[SYNC_CLOUD] Error updating record in table: %s", tableName);
                return false;
            }
        } else {
            // INSERT locally
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
            StringBuilder placeholders = new StringBuilder(" VALUES (");
            Object[] values = new Object[config.fields.size() + 4];

            sql.append(config.pk).append(", ");
            placeholders.append("?, ");
            values[0] = rmt.get(config.pk).getAsLong();

            int i = 1;
            for (String field : config.fields) {
                sql.append(field).append(", ");
                placeholders.append("?, ");
                values[i++] = rmt.has(field) && !rmt.get(field).isJsonNull() ? rmt.get(field).getAsString() : null;
            }

            sql.append("pb_id, pb_updated_at, pb_is_dirty)");
            placeholders.append("?, ?, ?)");
            values[i++] = pbId;
            values[i++] = updatedAt;
            values[i] = dirtyValue;

            try {
                db.execSQL(sql.toString() + placeholders.toString(), values);
                return true;
            } catch (SQLException e) {
                Timber.e(e, "[SYNC_CLOUD] Error inserting record into table: %s.%s", tableName, values[0]);
                return false;
            }
        }
    }

    private JsonObject getRemoteRecord(String tableName, String filter) throws IOException {
        Map<String, String> options = new HashMap<>();
        options.put("filter", filter);
        Response<JsonObject> response = mService.getRecords(tableName, options).execute();
        if (response.isSuccessful() && response.body() != null) {
            com.google.gson.JsonArray items = response.body().getAsJsonArray("items");
            if (items != null && !items.isEmpty()) {
                return items.get(0).getAsJsonObject();
            }
        }
        return null;
    }

    private boolean isUniqueValidationError(String errorBody) {
        if (TextUtils.isEmpty(errorBody)) return false;
        try {
            JsonObject errorObj = mGson.fromJson(errorBody, JsonObject.class);
            if (errorObj.has("data")) {
                JsonObject data = errorObj.getAsJsonObject("data");
                for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        JsonObject fieldError = entry.getValue().getAsJsonObject();
                        if (fieldError.has("code") && "validation_not_unique".equals(fieldError.get("code").getAsString())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void processTableDeletions(SupportSQLiteDatabase db, String tableName) throws IOException {
        String logTable = "pb_DELETED_RECORDS_LOG";
        Cursor cursor = db.query("SELECT * FROM " + logTable + " WHERE table_name = ?", new Object[]{tableName});
        if (cursor == null) return;

        try {
            int idIdx = cursor.getColumnIndex("id");
            int pbIdIdx = cursor.getColumnIndex("pb_id");

            while (cursor.moveToNext()) {
                if (idIdx == -1 || pbIdIdx == -1) continue;

                int logId = cursor.getInt(idIdx);
                String pbId = cursor.getString(pbIdIdx);

                // Use soft-delete on PocketBase as per sync_core.js logic
                JsonObject payload = new JsonObject();
                payload.addProperty("_is_deleted", 1);
                
                Response<JsonObject> response = mService.update(tableName, pbId, payload).execute();
                if (response.isSuccessful()) {
                    db.execSQL("DELETE FROM " + logTable + " WHERE id = ?", new Object[]{logId});
                }
            }
        } finally {
            cursor.close();
        }
    }


    private void updateLocalRecordAfterPushKeepingDirty(SupportSQLiteDatabase db, String tableName, String pkName, long localId, String newPbId) {
        db.execSQL("UPDATE " + tableName + " SET pb_is_dirty = 1, pb_id = ? WHERE " + pkName + " = ?",
                new Object[]{newPbId, localId});
    }

    private void updateLocalRecordAfterPush(SupportSQLiteDatabase db, String tableName, String pkName, long localId, String newPbId) {
        db.execSQL("UPDATE " + tableName + " SET pb_is_dirty = 0, pb_id = ? WHERE " + pkName + " = ?", 
                new Object[]{newPbId, localId});
    }

    // clear all sync engine data, like token, last sync time, etc.
    public void clearSyncEngine() {
        SyncPreferences prefs = new SyncPreferences(mContext);
        prefs.setPocketBaseSyncEnabled(false);
        prefs.setPocketBaseSyncLastSyncTimeToInitial();
        PocketBaseClient.getInstance(mContext).clearSession();
        new AppSettings(mContext).getDatabaseSettings().setDatabasePath("");
    }

}
