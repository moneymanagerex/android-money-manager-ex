package com.money.manager.ex.sync;

import android.content.Context;
import android.database.Cursor;
//import android.database.sqlite.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.utils.MmxFileUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            Timber.e(e, "Error loading table_config.json");
        }
    }

    /**
     * Orchestrates the sync cycle: Push followed by Pull.
     */
    public void synchronize() {
        if (!SyncManager.isCloudSyncEnabled() || mConfig == null) return;

        SupportSQLiteDatabase db = openHelper.get().getWritableDatabase();

        try {
            // Use exclusive transaction for the whole sync cycle
            db.beginTransaction();

            // 1. Push local changes
            for (String tableName : mConfig.SYNC_ORDER) {
                pushTableChanges(db, tableName);
            }

            // 2. Pull remote changes
            SyncPreferences prefs = new SyncPreferences(mContext);
            String lastSync = prefs.get("pb_last_sync_time", "1970-01-01 00:00:00.000Z");
            String syncStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.US).format(new Date());

            for (String tableName : mConfig.SYNC_ORDER) {
                pullTableChanges(db, tableName, lastSync);
            }

            prefs.set("pb_last_sync_time", syncStartTime);

            db.setTransactionSuccessful();
            Timber.i("Synchronization cycle completed.");
        } catch (Exception e) {
            Timber.e(e, "Error during synchronization");
        } finally {
            db.endTransaction();
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
        if (cursor == null) return;

        try {
            int pkIdx = cursor.getColumnIndex(config.pk);
            int pbIdIdx = cursor.getColumnIndex("pb_id");

            while (cursor.moveToNext()) {
                if (pkIdx == -1) continue;

                JsonObject payload = new JsonObject();
                for (String field : config.fields) {
                    int colIdx = cursor.getColumnIndex(field);
                    if (colIdx != -1 && !cursor.isNull(colIdx)) {
                        payload.addProperty(field, cursor.getString(colIdx));
                    }
                }

                String currentPbId = pbIdIdx != -1 ? cursor.getString(pbIdIdx) : null;
                long localId = cursor.getLong(pkIdx);

                Response<JsonObject> response;
                if (TextUtils.isEmpty(currentPbId)) {
                    response = mService.create(tableName, payload).execute();
                } else {
                    response = mService.update(tableName, currentPbId, payload).execute();
                }

                if (response.isSuccessful() && response.body() != null) {
                    String remoteId = response.body().get("id").getAsString();
                    updateLocalRecordAfterPush(db, tableName, config.pk, localId, remoteId);
                }
            }
        } finally {
            cursor.close();
        }
    }

    private void pullTableChanges(SupportSQLiteDatabase db, String tableName, String lastSync) throws IOException {
        TableConfig config = mConfig.SYNC_CONFIG.get(tableName);
        if (config == null) return;

        Map<String, String> options = new HashMap<>();
        options.put("filter", "updated > \"" + lastSync + "\"");
        options.put("sort", "updated");

        Response<JsonObject> response = mService.getRecords(tableName, options).execute();
        if (!response.isSuccessful() || response.body() == null) return;

        com.google.gson.JsonArray items = response.body().getAsJsonArray("items");
        for (JsonElement itemElement : items) {
            JsonObject rmt = itemElement.getAsJsonObject();
            String pbId = rmt.get("id").getAsString();
            String updatedAt = rmt.get("updated").getAsString();

            // Check if deleted in remote
            if (rmt.has("is_deleted") && rmt.get("is_deleted").getAsInt() == 1) {
                db.execSQL("DELETE FROM " + tableName + " WHERE pb_id = ?", new Object[]{pbId});
                continue;
            }

            // Check if exists locally
            Cursor localCursor = db.query("SELECT " + config.pk + " FROM " + tableName + " WHERE pb_id = ?", new Object[]{pbId});
            boolean existsLocally = localCursor != null && localCursor.moveToFirst();
            if (localCursor != null) localCursor.close();

            if (existsLocally) {
                // UPDATE locally with pb_is_dirty = 2 (ignore triggers)
                StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
                Object[] values = new Object[config.fields.size() + 2];
                int i = 0;
                for (String field : config.fields) {
                    sql.append(field).append(" = ?, ");
                    values[i++] = rmt.has(field) && !rmt.get(field).isJsonNull() ? rmt.get(field).getAsString() : null;
                }
                sql.append("pb_updated_at = ?, pb_is_dirty = 2 WHERE pb_id = ?");
                values[i++] = updatedAt;
                values[i] = pbId;
                db.execSQL(sql.toString(), values);
            } else {
                // INSERT locally with pb_is_dirty = 2
                StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
                StringBuilder placeholders = new StringBuilder(" VALUES (");
                Object[] values = new Object[config.fields.size() + 4]; // + pk, pb_id, pb_updated_at, pb_is_dirty

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
                placeholders.append("?, ?, 2)");
                values[i++] = pbId;
                values[i] = updatedAt;

                db.execSQL(sql.toString() + placeholders.toString(), values);
            }
        }

        // Reset pb_is_dirty to 0 for all pull-updated records
        db.execSQL("UPDATE " + tableName + " SET pb_is_dirty = 0 WHERE pb_is_dirty = 2");
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
                payload.addProperty("is_deleted", 1);
                
                Response<JsonObject> response = mService.update(tableName, pbId, payload).execute();
                if (response.isSuccessful()) {
                    db.execSQL("DELETE FROM " + logTable + " WHERE id = ?", new Object[]{logId});
                }
            }
        } finally {
            cursor.close();
        }
    }

    private void updateLocalRecordAfterPush(SupportSQLiteDatabase db, String tableName, String pkName, long localId, String newPbId) {
        db.execSQL("UPDATE " + tableName + " SET pb_is_dirty = 0, pb_id = ? WHERE " + pkName + " = ?", 
                new Object[]{newPbId, localId});
    }
}
